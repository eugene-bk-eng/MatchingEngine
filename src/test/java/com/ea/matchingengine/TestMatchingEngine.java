/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine;

import com.ea.matchingengine.feed.trade.DefaultTradeFeedMsg;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Collections;

public class TestMatchingEngine extends TestBase {

    private static final Logger logger = LogManager.getLogger(TestMatchingEngine.class);

    @Test
    public void testQuotePlacementAndOrdering() throws InterruptedException {

        engine.accept(buyLimitDayOrder(SYM_IBM, 300, 10.20));
        engine.processNextQueueMsg();
        engine.accept(buyLimitDayOrder(SYM_IBM, 100, 10.50));
        engine.processNextQueueMsg();
        engine.accept(buyLimitDayOrder(SYM_IBM, 200, 10.30));
        engine.processNextQueueMsg();
        // add another order on that level
        engine.accept(buyLimitDayOrder(SYM_IBM, 300, 10.50));
        engine.processNextQueueMsg();

        engine.accept(sellLimitDayOrder(SYM_IBM, 100, 15.00));
        engine.processNextQueueMsg();
        engine.accept(sellLimitDayOrder(SYM_IBM, 200, 15.10));
        engine.processNextQueueMsg();

        showBook(SYM_IBM);

        // construct expected book and verify
        // highest bid price to clients is on top.
        // this is the best price exch willing to buy at, followed by next best
        bidBook.add(makeQuote(SYM_IBM, 400, 10.50));
        bidBook.add(makeQuote(SYM_IBM, 200, 10.30));
        bidBook.add(makeQuote(SYM_IBM, 300, 10.20));
        assertBidBook(SYM_IBM, bidBook);

        // lowest offer price to clients is on top
        // this is the best (lowest) price exch willing to sell at, followed by next best (slightly higher)
        offerBook.add(makeQuote(SYM_IBM, 100, 15.0));
        offerBook.add(makeQuote(SYM_IBM, 200, 15.1));
        assertOfferBook(SYM_IBM, offerBook);
    }

    @Test
    public void testSimpleMatch() throws InterruptedException {

        engine.accept(buyLimitDayOrder(SYM_IBM, 100, 10.50));
        engine.processNextQueueMsg();

        assertBidBook(SYM_IBM, Lists.newArrayList(makeQuote(SYM_IBM, 100, 10.50)));
        assertOfferBook(SYM_IBM, Lists.newArrayList());

        engine.accept(sellLimitDayOrder(SYM_IBM, 100, 10.50));
        engine.processNextQueueMsg();

        showBook(SYM_IBM);

        // check both sides are empty again
        assertBidBook(SYM_IBM, Lists.newArrayList());
        assertOfferBook(SYM_IBM, Lists.newArrayList());

        // check trade has occurred
        tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(),SYM_IBM, 100, 10.50));
        assertBookTrades(tradesFeed, getTradeFeedMsgs(SYM_IBM));
    }

    @Test
    public void testMatchAndPost() throws InterruptedException {

        engine.accept(buyLimitDayOrder(SYM_IBM, 100, 15.00));
        engine.processNextQueueMsg(); // post on bid
        engine.accept(sellLimitDayOrder(SYM_IBM, 300, 10.00));
        engine.processNextQueueMsg(); // match, post on ask

        showBook(SYM_IBM);

        // check quote feed
        assertBidBook(SYM_IBM, Lists.newArrayList());
        assertOfferBook(SYM_IBM, Lists.newArrayList(makeQuote(SYM_IBM, 200, 10.00)));
        // check trade feed
        tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), SYM_IBM, 100, 12.50)); // price is improved for both
        assertBookTrades(tradesFeed, getTradeFeedMsgs(SYM_IBM));
    }

    @Test
    public void testMatchAndPostTwoSymbols() throws InterruptedException {

        engine.accept(buyLimitDayOrder(SYM_IBM, 100, 15.00));
        engine.processNextQueueMsg(); // post on bid
        engine.accept(sellLimitDayOrder(SYM_IBM, 300, 10.00));
        engine.processNextQueueMsg(); // match, post on ask

        engine.accept(sellLimitDayOrder(SYM_AAPL, 250, 10.20));
        engine.processNextQueueMsg();
        engine.accept(buyLimitDayOrder(SYM_AAPL, 100, 10.20));
        engine.processNextQueueMsg();

        showBook(SYM_IBM);

        showBook(SYM_AAPL);

        // check quote feed
        assertBidBook(SYM_IBM, Lists.newArrayList());
        assertOfferBook(SYM_IBM, Lists.newArrayList(makeQuote(SYM_IBM, 200, 10.00)));
        //
        assertOfferBook(SYM_IBM, Lists.newArrayList(makeQuote(SYM_IBM, 200, 10.00)));
        //
        assertOfferBook(SYM_AAPL, Lists.newArrayList(makeQuote(SYM_AAPL, 150, 10.20)));
        // check trade feed
        tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), SYM_IBM, 100, 12.50)); // price is improved for both
        tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), SYM_AAPL, 100, 10.20)); // price is improved for both
        assertBookTrades(tradesFeed, getTradeFeedMsgs());
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

        engine.accept(buyLimitDayOrder(SYM_IBM, 100, 10.5055667));
        engine.processNextQueueMsg();
        engine.accept(sellLimitDayOrder(SYM_IBM, 100, 11.789));
        engine.processNextQueueMsg();

        showBook(sym);

        // construct expected book and verify
        // highest bid price to clients is on top
        bidBook.add(makeQuote(SYM_IBM, 100, 10.51));
        assertBidBook(SYM_IBM, bidBook);

        // lowest offer price to clients is on top
        offerBook.add(makeQuote(SYM_IBM, 100, 11.79));
        assertOfferBook(SYM_IBM, offerBook);

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
            engine.accept(buyLimitDayOrder(SYM_IBM, 100, orderPrice));
            engine.processNextQueueMsg();
        }
        // sells go in the opposite direction, starting from the highest bid
        // bid is how much you are willing to pay for something
        int bla = 0;
        initialPrice = lastPrice;
        for (int i = 0; i < n; i++) {
            double orderPrice = initialPrice - i * delta;
            lastPrice = orderPrice;
            engine.accept(sellLimitDayOrder(SYM_IBM, 100, orderPrice));
            engine.processNextQueueMsg();
        }

        logger.info("\n" + engine.getQuoteFeed().getBook(sym));

        // book must be empty
        assertBidBook(SYM_IBM, bidBook);
        assertOfferBook(SYM_IBM, offerBook);

        // check trade feed
        initialPrice = 1.00;
        for (int i = 0; i < n; i++) {
            double matchPx = initialPrice + i * delta;
            tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), SYM_IBM, 100, matchPx)); // price is improved for both
        }
        // reverse the trades since they were matched from highest to lower price.
        Collections.reverse(tradesFeed);
        // check trade feed
        assertBookTrades(tradesFeed, getTradeFeedMsgs(sym));
    }

}
