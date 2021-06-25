package com.ea.matchingengine;

import com.ea.matchingengine.engine.MatchingEngine;
import com.ea.matchingengine.engine.MatchingEngineImpl;
import com.ea.matchingengine.feed.quote.DefaultQuoteMsg;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.quote.QuoteMsg;
import com.ea.matchingengine.feed.trade.TradeMsg;
import com.ea.matchingengine.fix.input.Order;
import com.ea.matchingengine.fix.input.OrderImpl;
import com.ea.matchingengine.fix.input.OrderSide;
import com.ea.matchingengine.fix.input.OrderType;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.List;

/**
 * @author : eugene
 * @created : 6/25/2021, Friday
 **/
public class TestBase {

    private static final Logger logger = LogManager.getLogger(TestMatchingEngine.class);
    MatchingEngine engine;
    public final List<QuoteMsg> bidBook = Lists.newArrayList(), offerBook = Lists.newArrayList();
    public final List<TradeMsg> tradesFeed = Lists.newArrayList();

    public final String SYM_IBM="ibm.n";
    public final String SYM_AAPL="aapl.q";

    @Before
    public void setup() {
        engine = new MatchingEngineImplForTest();
        engine.startMatching();
        bidBook.clear();
        offerBook.clear();
        tradesFeed.clear();
    }

    /// HELPFUL METHODS
    Order buyLimitDayOrder(String symbol, int size, double price) {
        return limitDayOrder(symbol, OrderSide.BUY, size, price);
    }

    Order sellLimitDayOrder(String symbol, int size, double price) {
        return limitDayOrder(symbol, OrderSide.SELL, size, price);
    }

    Order limitDayOrder(String symbol, OrderSide side, int size, double price) {
        return new OrderImpl(symbol, OrderType.LMT, side, size, price);
    }

    QuoteMsg makeQuote(String sym, int qty, double px) {
        return new DefaultQuoteMsg(System.nanoTime(), sym, qty, px);
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

    void assertBookTrades(List<TradeMsg> expectedRecords, List<TradeMsg> actualRecords) {
        Assert.assertEquals(expectedRecords.size(), actualRecords.size());
        for (int i = 0; i < expectedRecords.size(); i++) {
            TradeMsg a = expectedRecords.get(i);
            TradeMsg b = actualRecords.get(i);
            Assert.assertTrue( "expected:" + a + "\n" + "actual: " + b, a.equals(b) );
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

    @After
    public void tearDown() {
        engine.shutdown();
        engine = null;
    }

    public class MatchingEngineImplForTest extends MatchingEngineImpl {
        @Override
        public void initDispatch() {
        }
    }
}
