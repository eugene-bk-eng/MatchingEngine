/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.book;

import com.ea.matchingengine.feed.quote.FeedMsgSide;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.trade.TradeFeed;
import com.ea.matchingengine.fix.input.Amend;
import com.ea.matchingengine.fix.input.Cancel;
import com.ea.matchingengine.fix.input.Order;
import com.ea.matchingengine.fix.input.OrderSide;
import com.ea.matchingengine.fix.input.OrderType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BookImpl extends AbstractBook {

    final Map<BookKey, List<Order>> bidMap = new TreeMap(Comparator.<Double>naturalOrder().reversed()); // sorted by price, highest on top.
    final Map<BookKey, List<Order>> askMap = new TreeMap(Comparator.<Double>naturalOrder()); // sorted by price, lowest on top.

    public BookImpl(String bookSymbol, QuoteFeed bookFeed, TradeFeed tradeFeed) {
        super(bookSymbol, bookFeed, tradeFeed);
    }

    @Override
    public void match(Order order) {
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
     * @param order
     * @param mapMatch
     * @param restingBook
     * @param updateMatchedBookSide
     */
    void findMatch(Order order, Map<BookKey, List<Order>> mapMatch, Map<BookKey, List<Order>> restingBook, FeedMsgSide updateMatchedBookSide) {
        // look for match in ASK/OFFER
        for (Map.Entry<BookKey, List<Order>> e: mapMatch.entrySet()) {
            BookKey bookKey=e.getKey();
            double pxLevel = bookKey.getPrice();
            boolean matchOrder = isMatchable(updateMatchedBookSide,pxLevel, order.getPrice());
            if (matchOrder) {
                matchOrder(order, bookKey, e.getValue().iterator(), updateMatchedBookSide );
            }
        }

        // post remaining order quantity to book
        postOpenQty(order, restingBook, updateMatchedBookSide.flip());
    }

    private void matchOrder(Order order, BookKey bookKey, Iterator<Order> ordersOnLevelIter, FeedMsgSide updateMatchedBookSide) {
        // if resting order price
        int successfullyMatchedOrdersCnt=0;
        int beforeQtySum = 0, afterQtySum = 0;
        while (ordersOnLevelIter.hasNext()) {
            Order bookOrder = ordersOnLevelIter.next();
            if (order.getOpenQty() > 0) {
                // match order and create fix messages
                // trade feed is updated on each execution
                beforeQtySum += bookOrder.getOpenQty();
                if( matchAndCreateFix(order, bookOrder) ) {
                    successfullyMatchedOrdersCnt++;
                    afterQtySum += bookOrder.getOpenQty();
                    if (bookOrder.getOpenQty() == 0) {
                        // remove from the book
                        // TODO: if you replace with array, just set tombstone (null pointer)
                        ordersOnLevelIter.remove();
                    }
                }
            }
        }
        // update market feed for this price level.
        if( successfullyMatchedOrdersCnt>0 ) {
            quoteFeed.reportBookChange(updateMatchedBookSide, order.getSym(), bookKey, beforeQtySum, afterQtySum);
        }
    }

    /**
     * TODO: check NBBO prices
     * In US equity, to match incoming order to any level, matching price must be
     * within NBBO range as published by NASDAQ or NYSE for that stock.
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

    private void postOpenQty(Order order, Map<BookKey, List<Order>> restingBook, FeedMsgSide restingSide) {
        if (isPostable(order)) {
            if (order.getOpenQty() > 0) {
                // add or update some level
                if (restingBook.containsKey(new BookKey(order.getPrice()))) {
                    // add quantity on this level
                    int beforeQty = restingBook.get(new BookKey(order.getPrice())).stream().mapToInt(s -> s.getOpenQty()).sum();
                    // update level
                    restingBook.get(new BookKey(order.getPrice())).add(order);
                    // update market feed for this level.
                    int afterQty = beforeQty + order.getOpenQty();
                    quoteFeed.reportBookChange(restingSide, order.getSym(), new BookKey(order.getPrice()), beforeQty, afterQty);
                } else {
                    // add new level
                    restingBook.computeIfAbsent(new BookKey(order.getPrice()), p -> new ArrayList<Order>()).add(order);
                    // update market feed for this level.
                    int beforeQty = 0; // level had nothing
                    int afterQty = order.getOpenQty();
                    quoteFeed.reportBookChange(restingSide, order.getSym(), new BookKey(order.getPrice()), beforeQty, afterQty);
                }
            } else {
                // cancel back to client?
//                if (order.getOpenQty() > 0) {
//                    // order has open quantity, send unsolicited cxl back.
//                } else {
//                    // order is fully done. nothing is required
//                }
            }
        }
    }

    // TODO: is day and limit order
    private boolean isPostable(Order order) {
        if (order.getType() == OrderType.LMT) {
            return true;
        }
        return false;
    }

    private boolean matchAndCreateFix(Order incoming, Order rested) {
        String sym=incoming.getSym();
        int matchQty = Math.min(incoming.getOpenQty(), rested.getOpenQty());
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

        if( isMatchPriceWithinNBBO(sym, matchPx) ) {

            incoming.fillQty(matchQty);
            rested.fillQty(matchQty);

            // TODO: send fix execution back to client.

            // TODO:
            tradeFeed.reportTrade(incoming.getSym(), matchQty, matchPx);

            return true;
        }else{
            return false;
        }
    }

    // TODO: implement
    private  boolean isMatchPriceWithinNBBO(String sym, double matchPx) {
        return true;
    }

    @Override
    public void match(Cancel cancel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void match(Amend amend) {
        throw new UnsupportedOperationException();
    }
}
