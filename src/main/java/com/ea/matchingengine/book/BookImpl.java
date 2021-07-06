/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.book;

import com.ea.matchingengine.book.model.BookCancel;
import com.ea.matchingengine.book.model.BookOrder;
import com.ea.matchingengine.book.model.OrderId;
import com.ea.matchingengine.feed.quote.FeedMsgSide;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.trade.TradeFeed;
import com.ea.matchingengine.fix.client.FixAmend;
import com.ea.matchingengine.fix.client.OrderSide;
import com.ea.matchingengine.fix.client.OrderType;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BookImpl extends AbstractBook {

    private final Map<BookKey, List<BookOrder>> bidMap = new TreeMap(Comparator.<Double>naturalOrder().reversed()); // sorted by price, highest on top.
    private final Map<BookKey, List<BookOrder>> askMap = new TreeMap(Comparator.<Double>naturalOrder()); // sorted by price, lowest on top.

    private final Map<OrderId, BookKey> lookupBidOrder = Maps.newHashMap();
    private final Map<OrderId, BookKey> lookupAskOrder = Maps.newHashMap();

    public BookImpl(String bookSymbol, QuoteFeed bookFeed, TradeFeed tradeFeed) {
        super(bookSymbol, bookFeed, tradeFeed);
    }

    @Override
    public void match(BookOrder order) {
        switch (order.getSide()) {
            case BUY:
                // matching client buy to OFFER side
                findMatch(order, askMap, bidMap, FeedMsgSide.OFFER);
                break;
            case SELL:
                // matching client sell to BID side
                findMatch(order, bidMap, askMap, FeedMsgSide.BID);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Match order
     *
     * @param order
     * @param mapMatch
     * @param restingBook
     * @param updateMatchedBookSide
     */
    void findMatch(BookOrder order, Map<BookKey, List<BookOrder>> mapMatch, Map<BookKey, List<BookOrder>> restingBook, FeedMsgSide updateMatchedBookSide) {
        // look for match in ASK/OFFER
        for (Map.Entry<BookKey, List<BookOrder>> e : mapMatch.entrySet()) {
            BookKey bookKey = e.getKey();
            double pxLevel = bookKey.getPrice();
            boolean matchOrder = isMatchable(updateMatchedBookSide, pxLevel, order.getPrice());
            if (matchOrder) {
                matchOrder(order, bookKey, e.getValue().iterator(), updateMatchedBookSide);
            }
        }

        // post remaining order quantity to book
        postOpenQty(order, restingBook, updateMatchedBookSide.flip());
    }

    private void matchOrder(BookOrder order, BookKey bookKey, Iterator<BookOrder> ordersOnLevelIter, FeedMsgSide updateMatchedBookSide) {
        // if resting order price
        int successfullyMatchedOrdersCnt = 0;
        int beforeQtySum = 0, afterQtySum = 0;
        while (ordersOnLevelIter.hasNext()) {
            BookOrder bookOrder = ordersOnLevelIter.next();
            if (order.getBookQty() > 0) {
                // match order and create fix messages
                // trade feed is updated on each execution
                beforeQtySum += bookOrder.getBookQty();
                if (matchAndCreateFix(order, bookOrder)) {
                    successfullyMatchedOrdersCnt++;
                    afterQtySum += bookOrder.getBookQty();
                    if (bookOrder.getBookQty() == 0) {
                        // remove from the book
                        ordersOnLevelIter.remove();
                    }
                }
            }
        }
        // update market feed for this price level.
        if (successfullyMatchedOrdersCnt > 0) {
            quoteFeed.reportBookChange(updateMatchedBookSide, order.getSym(), bookKey, beforeQtySum, afterQtySum);
        }
    }

    /**
     * TODO: check NBBO prices
     * In US equity, to match incoming order to any level, matching price must be
     * within NBBO range as published by NASDAQ or NYSE for that stock.
     *
     * @param updateMatchedBookSide
     * @param pxLevel
     * @param orderPrice
     * @return
     */
    private boolean isMatchable(FeedMsgSide updateMatchedBookSide, double pxLevel, double orderPrice) {
        if (updateMatchedBookSide == FeedMsgSide.OFFER && pxLevel <= orderPrice) {
            // if resting order price is less than incoming buy then we can give price improvement to both
            return true;
        }
        if (updateMatchedBookSide == FeedMsgSide.BID && pxLevel >= orderPrice) {
            // if resting order price is higher than incoming sell then we can give price improvement to both
            return true;
        }
        return false;
    }

    private void postOpenQty(BookOrder order, Map<BookKey, List<BookOrder>> restingBook, FeedMsgSide restingSide) {
        if (isPostable(order)) {
            if (order.getBookQty() > 0) {
                // add or update some level
                if (restingBook.containsKey(new BookKey(order.getPrice()))) {
                    // add quantity on this level
                    int beforeQty = restingBook.get(new BookKey(order.getPrice())).stream().mapToInt(s -> s.getBookQty()).sum();
                    // update level
                    restingBook.get(new BookKey(order.getPrice())).add(order);
                    // update market feed for this level.
                    int afterQty = beforeQty + order.getBookQty();
                    quoteFeed.reportBookChange(restingSide, order.getSym(), new BookKey(order.getPrice()), beforeQty, afterQty);
                } else {
                    // add new level
                    restingBook.computeIfAbsent(new BookKey(order.getPrice()), p -> new ArrayList<BookOrder>()).add(order);
                    // update market feed for this level.
                    int beforeQty = 0; // level had nothing
                    int afterQty = order.getBookQty();
                    quoteFeed.reportBookChange(restingSide, order.getSym(), new BookKey(order.getPrice()), beforeQty, afterQty);
                }
            }
        }
    }

    // TODO: is day and limit order
    private boolean isPostable(BookOrder order) {
        if (order.getType() == OrderType.LMT) {
            return true;
        }
        return false;
    }

    private boolean matchAndCreateFix(BookOrder incoming, BookOrder rested) {
        String sym = incoming.getSym();
        int matchQty = Math.min(incoming.getBookQty(), rested.getBookQty());
        // validation
        if (incoming.getType() == OrderType.LMT && rested.getType() == OrderType.LMT) {
            // buyer price is equal or greater than seller's
            if (incoming.getSide() == OrderSide.BUY) {
                if (incoming.getPrice() < rested.getPrice()) {
                    throw new IllegalStateException("Buy order px>Sell order px");
                }
            } else {
                // incoming is a sell, resting is a buy order.
                if (rested.getPrice() < incoming.getPrice()) {
                    throw new IllegalStateException("Buy order px>Sell order px");
                }
            }
        }
        // match
        double matchPx = rested.getPrice();
        if (rested.getPrice() != incoming.getPrice()) {
            // mid point between prices, giving both price improvement
            matchPx = Math.min(rested.getPrice(), incoming.getPrice()) + Math.abs(rested.getPrice() - incoming.getPrice()) / 2;
        }

        if (isMatchPriceWithinNBBO(sym, matchPx)) {

            incoming.consumeQty(matchQty);
            rested.consumeQty(matchQty);

            // TODO: send fix execution back to client.

            // TODO:
            tradeFeed.reportTrade(incoming.getSym(), matchQty, matchPx);

            return true;
        } else {
            return false;
        }
    }

    // TODO: implement
    private boolean isMatchPriceWithinNBBO(String sym, double matchPx) {
        return true;
    }

    @Override
    public void match(BookCancel bookCancel) {
        // TODO: create quick map lookup. this is a dumb linear implementation

        // look in bids
        for (Map.Entry<BookKey, List<BookOrder>> e : bidMap.entrySet()) {
            BookKey key = e.getKey();
            Iterator<BookOrder> t = e.getValue().iterator();
            while (t.hasNext()) {
                BookOrder order = t.next();
                if (order.getOrderId().equals(bookCancel.getOrderId())) {
                    // cancel this level, update market feed
                    int beforeQty = bidMap.get(key).stream().mapToInt(s -> s.getBookQty()).sum();
                    int afterQty = beforeQty - order.getBookQty();
                    quoteFeed.reportBookChange(FeedMsgSide.BID, order.getSym(), key, beforeQty, afterQty);
                    // remove order
                    t.remove();
                    // out
                    break;
                }
            }
        }

        for (Map.Entry<BookKey, List<BookOrder>> e : askMap.entrySet()) {
            BookKey key = e.getKey();
            Iterator<BookOrder> t = e.getValue().iterator();
            while (t.hasNext()) {
                BookOrder order = t.next();
                if (order.getOrderId().equals(bookCancel.getOrderId())) {
                    // cancel this level, update market feed
                    int beforeQty = askMap.get(key).stream().mapToInt(s -> s.getBookQty()).sum();
                    int afterQty = beforeQty - order.getBookQty();
                    quoteFeed.reportBookChange(FeedMsgSide.OFFER, order.getSym(), key, beforeQty, afterQty);
                    // remove order
                    t.remove();
                    // out
                    break;
                }
            }
        }

    }
}
