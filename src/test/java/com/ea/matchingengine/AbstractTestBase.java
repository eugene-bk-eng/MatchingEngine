package com.ea.matchingengine;

import com.ea.matchingengine.engine.MatchingEngine;
import com.ea.matchingengine.engine.MatchingEngineImpl;
import com.ea.matchingengine.feed.quote.DefaultQuoteMsg;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.quote.QuoteMsg;
import com.ea.matchingengine.feed.trade.DefaultTradeFeedMsg;
import com.ea.matchingengine.feed.trade.TradeMsg;
import com.ea.matchingengine.fix.client.FixCancel;
import com.ea.matchingengine.fix.client.FixCancelImpl;
import com.ea.matchingengine.fix.client.FixOrder;
import com.ea.matchingengine.fix.client.FixOrderImpl;
import com.ea.matchingengine.fix.client.OrderSide;
import com.ea.matchingengine.fix.client.OrderType;
import com.ea.matchingengine.testutils.TestLogger;
import com.google.common.collect.Lists;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/
public abstract class AbstractTestBase {

    private Logger logger = null;
    public MatchingEngine engine;
    //public final List<QuoteMsg> bidBook = Lists.newArrayList(), offerBook = Lists.newArrayList();
    public final List<TradeMsg> tradesFeed = Lists.newArrayList();

    //
    public final String SYM_IBM = "ibm.n";
    public final String SYM_AAPL = "aapl.q";

    @Before
    public void setup() {
        TestLogger.createCustomLoggers();
        logger = LogManager.getLogger(LoggerNames.getAppLoggerName());
        org.apache.commons.configuration2.Configuration config = new MapConfiguration(new HashMap());
        engine = new MatchingEngineImplForTest(config);
        engine.startMatching();
        tradesFeed.clear();
    }

    @After
    public void tearDown() {
        engine.shutdown();
        engine = null;
        TestLogger.destroyCustomLoggers();
    }

    public void cancelOrder(FixOrder order) throws InterruptedException {
        FixCancel cancel = new FixCancelImpl(order.getClientId(), order.getOrderId(), order.getSym());
        engine.accept(cancel);
        engine.waitAndProcessNextMsg();
    }

//    public Amend amendOrder(FixOrder order, int new_size, double new_price) throws InterruptedException {
//        Amend amend=new AmendImpl(order.getId(), order.getSym(),order.getType(),order.getSide(), new_size, new_price, order.getPrice() );
//        engine.accept(amend);
//        engine.waitAndProcessNextMsg();
//        return amend;
//    }

    /**
     * @param symbol
     * @param size
     * @param price
     * @throws InterruptedException
     */
    public FixOrder lmtBuy(String clientId, String symbol, int size, double price) throws InterruptedException {
        return submitOrder(clientId, symbol, OrderType.LMT, OrderSide.BUY, size, price);
    }

    /**
     * @param symbol
     * @param size
     * @param price
     * @throws InterruptedException
     */
    public FixOrder lmtSell(String clientId, String symbol, int size, double price) throws InterruptedException {
        return submitOrder(clientId, symbol, OrderType.LMT, OrderSide.SELL, size, price);
    }

    /**
     * This method sends the order to matching engine(ME) AND
     * synchronously invokes ME's process order thread.
     *
     * @param symbol
     * @param type
     * @param side
     * @param size
     * @param price
     * @throws InterruptedException
     */
    public FixOrder submitOrder(String clientId, String symbol, OrderType type, OrderSide side, int size, double price) throws InterruptedException {
        FixOrder order = null;
        if (type == OrderType.LMT) {
            if (side == OrderSide.BUY) {
                order = buyLimitDayOrder(clientId, symbol, size, price);
                engine.accept(order);
            } else if (side == OrderSide.SELL) {
                order = sellLimitDayOrder(clientId, symbol, size, price);
                engine.accept(order);
            } else {
                throw new IllegalStateException();
            }

            engine.waitAndProcessNextMsg();
        } else {
            throw new IllegalStateException();
        }
        return order;
    }

    public FixOrder sendOrder(String clientId, String symbol, int size, double price) throws InterruptedException {
        FixOrder order = buyLimitDayOrder(clientId, symbol, size, price);
        engine.accept(order);
        engine.waitAndProcessNextMsg();
        return order;
    }

    /// HELPFUL METHODS
    FixOrder buyLimitDayOrder(String clientId, String symbol, int size, double price) {
        return limitDayOrder(clientId, symbol, OrderSide.BUY, size, price);
    }

    FixOrder sellLimitDayOrder(String clientId, String symbol, int size, double price) {
        return limitDayOrder(clientId, symbol, OrderSide.SELL, size, price);
    }

    FixOrder limitDayOrder(String clientId, String symbol, OrderSide side, int size, double price) {
        return new FixOrderImpl(clientId, symbol, OrderType.LMT, side, size, price);
    }

    QuoteMsg makeQuote(String sym, int qty, double px) {
        return new DefaultQuoteMsg(System.nanoTime(), sym, qty, px);
    }

    void assertBookBidEmptyAskNotEmpty(String sym, String askSize_s, String askPx_s) {
        int askSize[] = Arrays.stream(askSize_s.split("[\\s,]+")).mapToInt(s -> Integer.parseInt(s)).toArray();
        double askPx[] = Arrays.stream(askPx_s.split("[\\s,]+")).mapToDouble(s -> Double.parseDouble(s)).toArray();
        assertBook(sym, null, null, askSize, askPx);
    }

    void assertBookBidNotEmptyAskEmpty(String sym, String bidSize_s, String bidPx_s) {
        int bidSize[] = Arrays.stream(bidSize_s.split("[\\s,]+")).mapToInt(s -> Integer.parseInt(s)).toArray();
        double bidPx[] = Arrays.stream(bidPx_s.split("[\\s,]+")).mapToDouble(s -> Double.parseDouble(s)).toArray();
        assertBook(sym, bidSize, bidPx, null, null);
    }

    void assertBookBidAskEmpty(String sym) {
        assertBook(sym, null, new double[]{}, null, new double[]{});
    }

    /**
     * Yet another format
     *
     * @param sym
     * @param entries
     */
    void assertBook(String sym, String... entries) {
        List<QuoteMsg> expectedBidBook = Lists.newArrayList();
        List<QuoteMsg> expectedAskBook = Lists.newArrayList();
        for (String e : entries) {
            if (e.strip().endsWith("|")) {
                // BID side
                if (!e.strip().toLowerCase().contains("empty")) {
                    QuoteMsg quote = parse(sym, e.strip().replace("|", ""));
                    expectedBidBook.add(quote);
                }
            } else if (e.strip().startsWith("|")) {
                // OFFER side
                if (!e.strip().toLowerCase().contains("empty")) {
                    QuoteMsg quote = parse(sym, e.strip().replace("|", ""));
                    expectedAskBook.add(quote);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        //
        Collections.reverse(expectedAskBook);
        assertBook(expectedBidBook, getQuoteFeed().getBids(sym));
        assertBook(expectedAskBook, getQuoteFeed().getOffers(sym));
    }

    QuoteMsg parse(String sym, String entry) {
        QuoteMsg quote = null;
        int pos = entry.indexOf("x");
        Assert.assertTrue(pos > 0); // check correct format
        int qty = Integer.parseInt(entry.substring(0, pos).strip());
        double px = Double.parseDouble(entry.substring(pos + 1).strip());
        return new DefaultQuoteMsg(0, sym, qty, px);
    }

    // convieniece method.. java syntax is annoyingly verbose
    void assertBook(String sym, String bidSize_s, String bidPx_s, String askSize_s, String askPx_s) {
        int bidSize[] = Arrays.stream(bidSize_s.split("[\\s,]+")).mapToInt(s -> Integer.parseInt(s)).toArray();
        double bidPx[] = Arrays.stream(bidPx_s.split("[\\s,]+")).mapToDouble(s -> Double.parseDouble(s)).toArray();
        int askSize[] = Arrays.stream(askSize_s.split("[\\s,]+")).mapToInt(s -> Integer.parseInt(s)).toArray();
        double askPx[] = Arrays.stream(askPx_s.split("[\\s,]+")).mapToDouble(s -> Double.parseDouble(s)).toArray();
        assertBook(sym, bidSize, bidPx, askSize, askPx);
    }

    void assertBook(String sym, int bidSize[], double bidPx[], int askSize[], double askPx[]) {
        if (bidSize == null || bidSize.length == 0) {
            Assert.assertEquals(0, getQuoteFeed().getBids(sym).size()); // empty book
        } else {
            List<QuoteMsg> expectedBook = Lists.newArrayList();
            for (int i = 0; i < bidSize.length; i++) {
                expectedBook.add(makeQuote(sym, bidSize[i], bidPx[i]));
            }
            assertBook(expectedBook, getQuoteFeed().getBids(sym));
        }
        if (askSize == null || askPx.length == 0) {
            Assert.assertEquals(0, getQuoteFeed().getOffers(sym).size()); // empty book
        } else {
            List<QuoteMsg> expectedBook = Lists.newArrayList();
            for (int i = 0; i < askSize.length; i++) {
                expectedBook.add(makeQuote(sym, askSize[i], askPx[i]));
            }
            assertBook(expectedBook, getQuoteFeed().getOffers(sym));
        }
    }

    void assertBidBook(String sym, List<QuoteMsg> expectedRecords) {
        assertBook(expectedRecords, getQuoteFeed().getBids(sym));
    }

    void assertOfferBook(String sym, List<QuoteMsg> expectedRecords) {
        assertBook(expectedRecords, getQuoteFeed().getOffers(sym));
    }

    void assertBook(List<QuoteMsg> expectedRecords, List<QuoteMsg> actualRecords) {
        Assert.assertEquals(expectedRecords.size(), actualRecords.size());
        for (int i = 0; i < expectedRecords.size(); i++) {
            Assert.assertEquals(expectedRecords.get(i), actualRecords.get(i));
        }
    }

    /**
     * @param expected
     */
    void assertTrades(String... expected) {
        List<TradeMsg> expectedTrades = Lists.newArrayList();
        for (String s : expected) {
            String parts[] = s.strip().split("[\\s,]+");
            String sym = parts[0];
            int tradeQty = Integer.parseInt(parts[1]);
            Double tradePx = Double.parseDouble(parts[2]);
            expectedTrades.add(new DefaultTradeFeedMsg(0, sym, tradeQty, tradePx));
        }
        assertTrades(expectedTrades, getTradeFeedMsgs());
    }

    void assertTrades(List<TradeMsg> expectedRecords, List<TradeMsg> actualRecords) {
        Assert.assertEquals(expectedRecords.size(), actualRecords.size());
        for (int i = 0; i < expectedRecords.size(); i++) {
            TradeMsg a = expectedRecords.get(i);
            TradeMsg b = actualRecords.get(i);
            Assert.assertTrue("expected:" + a + "\n" + "actual: " + b, a.equals(b));
        }
    }

    QuoteFeed getQuoteFeed() {
        return engine.getQuoteFeed();
    }

    List<TradeMsg> getTradeFeedMsgs() {
        return engine.getTradeFeed().getLastTrades();
    }

    List<TradeMsg> getTradeFeedMsgs(String symbol) {
        return engine.getTradeFeed().getLastTrades(symbol);
    }

    public void showBook(String symbol) {
        logger.info("\n" + engine.getQuoteFeed().getBook(symbol));
    }

    public class MatchingEngineImplForTest extends MatchingEngineImpl {

        public MatchingEngineImplForTest(Configuration config) {
            super(config);
        }

        @Override
        public void initDispatch() {
        }
    }
}
