/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine;

import com.ea.matchingengine.engine.MatchingEngine;
import com.ea.matchingengine.engine.MatchingEngineImpl;
import com.google.common.collect.Lists;
import com.ea.matchingengine.feed.quote.DefaultQuoteMsg;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.quote.QuoteMsg;
import com.ea.matchingengine.feed.trade.DefaultTradeFeedMsg;
import com.ea.matchingengine.feed.trade.TradeMsg;
import com.ea.matchingengine.fix.input.Order;
import com.ea.matchingengine.fix.input.OrderImpl;
import com.ea.matchingengine.fix.input.OrderSide;
import com.ea.matchingengine.fix.input.OrderType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class TestMatchingEngine {

    private static final Logger logger = LogManager.getLogger(TestMatchingEngine.class);
    MatchingEngine engine;
    List<QuoteMsg> bidBook = Lists.newArrayList(), offerBook = Lists.newArrayList();
    List<TradeMsg> tradesFeed = Lists.newArrayList();

    @Before
    public void setup() {
        engine = new MatchingEngineImplForTest();
        bidBook.clear();
        offerBook.clear();
        tradesFeed.clear();
    }

    public void docTestingPlan() {
        // confirm book is create correctlly
    }

    @Test
    public void testQuotePlacementAndOrdering() throws InterruptedException {
        logger.log(Level.INFO, "testMatching");

        String sym = "ibm.n";

        engine.accept(createBuyLmt(sym, 300, 10.20));
        engine.processQueue();
        engine.accept(createBuyLmt(sym, 100, 10.50));
        engine.processQueue();
        engine.accept(createBuyLmt(sym, 200, 10.30));
        engine.processQueue();

        engine.accept(createSellLmt(sym, 100, 15.00));
        engine.processQueue();
        engine.accept(createSellLmt(sym, 200, 15.10));
        engine.processQueue();

        showBook(sym);

        // construct expected book and verify
        // highest bid price to clients is on top.
        // this is the best price exch willing to buy at, followed by next best
        bidBook.add(makeQuote(sym, 100, 10.50));
        bidBook.add(makeQuote(sym, 200, 10.30));
        bidBook.add(makeQuote(sym, 300, 10.20));
        assertBidBook(sym, bidBook);

        // lowest offer price to clients is on top
        // this is the best (lowest) price exch willing to sell at, followed by next best (slightly higher)
        offerBook.add(makeQuote(sym, 100, 15.0));
        offerBook.add(makeQuote(sym, 200, 15.1));
        assertOfferBook(sym, offerBook);
    }

    @Test
    public void testSimpleMatch() throws InterruptedException {
        logger.log(Level.INFO, "testMatching");

        String sym = "ibm.n";

        engine.accept(createBuyLmt(sym, 100, 10.50));
        engine.processQueue();

        assertBidBook(sym, Lists.newArrayList(makeQuote(sym, 100, 10.50)));
        assertOfferBook(sym, Lists.newArrayList());

        engine.accept(createSellLmt(sym, 100, 10.50));
        engine.processQueue();

        showBook(sym);

        // check both sides are empty again
        assertBidBook(sym, Lists.newArrayList());
        assertOfferBook(sym, Lists.newArrayList());

        // check trade has occurred
        tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(),sym, 100, 10.50));
        assertBookTrades(tradesFeed, getTradeFeedMsg(sym));
    }

    @Test
    public void testMatchAndPost() throws InterruptedException {
        logger.log(Level.INFO, "testMatching");

        String sym = "ibm.n";

        engine.accept(createBuyLmt(sym, 100, 15.00));
        engine.processQueue(); // post on bid
        engine.accept(createSellLmt(sym, 300, 10.00));
        engine.processQueue(); // match, post on ask

        showBook(sym);

        // check quote feed
        assertBidBook(sym, Lists.newArrayList());
        assertOfferBook(sym, Lists.newArrayList(makeQuote(sym, 200, 10.00)));
        // check trade feed
        tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), sym, 100, 12.50)); // price is improved for both
        assertBookTrades(tradesFeed, getTradeFeedMsg(sym));
    }

    /**
     * you must normalize prices to two or four decimals
     * in order to use them reliably in book map as keys.
     * you can't pass the actual double price as is because you will end up
     * creating a lot of levels because client can send up to ~13 decimals after comma
     * hence all exchanges standardize input prices to 2 or 4 decimals
     * and trade prices to ~6. that's because price improvement and tight spread
     * may require higher precision.
     *
     * @throws InterruptedException
     */
    @Test
    public void testPriceNormalizations() throws InterruptedException {

        String sym = "ibm.n";

        engine.accept(createBuyLmt(sym, 100, 10.5055667));
        engine.processQueue();
        engine.accept(createSellLmt(sym, 100, 11.789));
        engine.processQueue();

        showBook(sym);

        // construct expected book and verify
        // highest bid price to clients is on top
        bidBook.add(makeQuote(sym, 100, 10.51));
        assertBidBook(sym, bidBook);

        // lowest offer price to clients is on top
        offerBook.add(makeQuote(sym, 100, 11.79));
        assertOfferBook(sym, offerBook);

        showBook(sym);
    }

    /**
     * You have to coordinate buys and sell prices.
     * Otherwise the book will price improve and writing a test is harder
     * @throws InterruptedException
     */
    @Test
    public void testLoop() throws InterruptedException {

        String sym = "ibm.n";

        int n = 1200;
        double initialPrice = 1.00;
        double delta = 0.01;
        double lastPrice = 0;
        for (int i = 0; i < n; i++) {
            double orderPrice = initialPrice + i * delta;
            lastPrice = orderPrice;
            engine.accept(createBuyLmt(sym, 100, orderPrice));
            engine.processQueue();
        }
        // sells go in the opposite direction, starting from the highest bid
        // bid is how much you are willing to pay for something
        int bla = 0;
        initialPrice = lastPrice;
        for (int i = 0; i < n; i++) {
            double orderPrice = initialPrice - i * delta;
            lastPrice = orderPrice;
            engine.accept(createSellLmt(sym, 100, orderPrice));
            engine.processQueue();
        }

        logger.info("\n" + engine.getQuoteFeed().getBook(sym));

        // book must be empty
        assertBidBook(sym, bidBook);
        assertOfferBook(sym, offerBook);

        // check trade feed
        initialPrice = 1.00;
        for (int i = 0; i < n; i++) {
            double matchPx = initialPrice + i * delta;
            tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), sym, 100, matchPx)); // price is improved for both
        }
        // reverse the trades since they were matched from highest to lower price.
        Collections.reverse(tradesFeed);
        // check trade feed
        assertBookTrades(tradesFeed, getTradeFeedMsg(sym));
    }


    /// HELPFUL METHODS
    Order createBuyLmt(String symbol, int size, double price) {
        return createLmtOrder(symbol, OrderSide.BUY, size, price);
    }

    Order createSellLmt(String symbol, int size, double price) {
        return createLmtOrder(symbol, OrderSide.SELL, size, price);
    }

    Order createLmtOrder(String symbol, OrderSide side, int size, double price) {
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

    List<TradeMsg> getTradeFeedMsg(String symbol) {
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

    class MatchingEngineImplForTest extends MatchingEngineImpl {

        @Override
        public void initDispatch() {
        }
    }
}
