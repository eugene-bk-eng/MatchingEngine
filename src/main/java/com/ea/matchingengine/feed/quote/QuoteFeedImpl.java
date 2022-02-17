package com.ea.matchingengine.feed.quote;

/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 * <p>
 * Book feed sends
 * NEW - order added at a level.
 * UPDATE - order level is updated, must be price
 * DELETE - order level is removed.
 * <p>
 * Actual feed message include:
 * TYPE     Symbol  Side    Qty     Price
 * N/U/D    IBM     BID     100     10.50
 * <p>
 * Book feed sends
 * NEW - order added at a level.
 * UPDATE - order level is updated, must be price
 * DELETE - order level is removed.
 * <p>
 * Actual feed message include:
 * TYPE     Symbol  Side    Qty     Price
 * N/U/D    IBM     BID     100     10.50
 * <p>
 * Book feed sends
 * NEW - order added at a level.
 * UPDATE - order level is updated, must be price
 * DELETE - order level is removed.
 * <p>
 * Actual feed message include:
 * TYPE     Symbol  Side    Qty     Price
 * N/U/D    IBM     BID     100     10.50
 */

/**
 * Book feed sends
 * NEW - order added at a level.
 * UPDATE - order level is updated, must be price
 * DELETE - order level is removed.
 *
 * Actual feed message include:
 * TYPE     Symbol  Side    Qty     Price
 * N/U/D    IBM     BID     100     10.50
 *
 */

import com.ea.matchingengine.LoggerNames;
import com.ea.matchingengine.book.BookKey;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feed is supposed to publish to some messaging bus asynchronously via another thread(s)
 */

public class QuoteFeedImpl extends AbstractQuoteFeed {

    private final Logger logger = LogManager.getLogger(LoggerNames.getAppLoggerName());
    Map<String, Map<BookKey, QuoteMsg>> bids = Maps.newHashMap(); // sorted by price, highest on top.
    Map<String, Map<BookKey, QuoteMsg>> offers = Maps.newHashMap(); // sorted by price, lowest on top.
    private BookPrinter bookPrinter;

    public QuoteFeedImpl() {
        bookPrinter = new BookPrinter();
    }

    @Override
    public void reportBookChange(FeedMsgSide bookSide, String symbol, BookKey px_level, int beforeQty, int afterQty) {
        if (afterQty == 0) {
            // delete level. cancelled or matched.
            logger.info(String.format("DELETE %s LEVEL px: %s, before: %s, after: %s", bookSide.getValue(), px_level, beforeQty, afterQty));
            if (bookSide == FeedMsgSide.BID) {
                bids.computeIfAbsent(symbol, p -> new HashMap<>()).remove(px_level);
            } else if (bookSide == FeedMsgSide.OFFER) {
                offers.computeIfAbsent(symbol, p -> new HashMap<>()).remove(px_level);
            }
        } else if (afterQty > 0 && afterQty < beforeQty) {
            // updating existing level, decreasing qty
            logger.info(String.format("UPDATE %s LEVEL px: %s, before: %s, after: %s", bookSide.getValue(), px_level, beforeQty, afterQty));
            QuoteMsg record = new DefaultQuoteMsg(System.nanoTime(), symbol, afterQty, px_level.getPrice());
            if (bookSide == FeedMsgSide.BID) {
                bids.get(symbol).put(px_level, record);
            } else if (bookSide == FeedMsgSide.OFFER) {
                offers.get(symbol).put(px_level, record);
            }
        } else if (beforeQty > 0 && afterQty > beforeQty) {
            // updating existing level, increasing qty
            logger.info(String.format("UPDATE %s LEVEL px: %s, before: %s, after: %s", bookSide.getValue(), px_level, beforeQty, afterQty));
            QuoteMsg record = new DefaultQuoteMsg(System.nanoTime(), symbol, afterQty, px_level.getPrice());
            if (bookSide == FeedMsgSide.BID) {
                bids.get(symbol).put(px_level, record);
            } else if (bookSide == FeedMsgSide.OFFER) {
                offers.get(symbol).put(px_level, record);
            }
        } else if (beforeQty == 0 && afterQty > 0) {
            // new level
            logger.info(String.format("NEW %s LEVEL px: %s, before: %s, after: %s", bookSide.getValue(), px_level, beforeQty, afterQty));
            QuoteMsg record = new DefaultQuoteMsg(System.nanoTime(), symbol, afterQty, px_level.getPrice());
            if (bookSide == FeedMsgSide.BID) {
                bids.computeIfAbsent(symbol, p -> new HashMap<>()).put(px_level, record);
            } else if (bookSide == FeedMsgSide.OFFER) {
                offers.computeIfAbsent(symbol, p -> new HashMap<>()).put(px_level, record);
            }
        } else if (afterQty == beforeQty) {
            // do nothing
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String getBook(String symbol) {
        return bookPrinter.getBook(symbol, bids, offers);
    }

    @Override
    public List<QuoteMsg> getBids(String symbol) {

        List<QuoteMsg> list = Lists.newArrayList();
        List<BookKey> prices = Lists.newArrayList(bids.computeIfAbsent(symbol, p -> new HashMap<>()).keySet());
        prices.sort(Comparator.reverseOrder());
        for (BookKey px : prices) {
            list.add(bids.get(symbol).get(px));
        }

        return list;
    }

    @Override
    public List<QuoteMsg> getOffers(String symbol) {
        List<QuoteMsg> list = Lists.newArrayList();
        List<BookKey> prices = Lists.newArrayList(offers.computeIfAbsent(symbol, p -> new HashMap<>()).keySet());
        prices.sort(Comparator.naturalOrder());
        for (BookKey px : prices) {
            list.add(offers.get(symbol).get(px));
        }
        return list;
    }

}
