/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine;

import com.ea.matchingengine.feed.trade.DefaultTradeFeedMsg;
import com.ea.matchingengine.fix.client.FixOrder;
import com.ea.matchingengine.testutils.TestLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Collections;

public class TestMatching extends AbstractTestBase {

    private final Logger logger = LogManager.getLogger(LoggerNames.getAppLoggerName());

    @Test
    public void testQuotePlacementAndOrdering() throws InterruptedException {

        logger.info("testQuotePlacementAndOrdering");

        lmtBuy("C1", SYM_IBM, 300, 10.20);
        lmtBuy("C1", SYM_IBM, 100, 10.50);
        lmtBuy("C1", SYM_IBM, 200, 10.30);
        lmtBuy("C1", SYM_IBM, 300, 10.50); // 2nd order on level

        lmtSell("C1", SYM_IBM, 100, 15.00);
        lmtSell("C1", SYM_IBM, 200, 15.10);

        showBook(SYM_IBM);

        // highest bid price to clients is on top.
        // this is the best price exch willing to buy at, followed by next best

        // lowest offer price to clients is on top
        // this is the best (lowest) price exch willing to sell at, followed by next best (slightly higher)
        assertBook(SYM_IBM, "           |200 x 15.1",
                "           |100 x 15.0",
                "400 x 10.5 |          ",
                "200 x 10.3 |          ",
                "300 x 10.2 |          ");

        String logLines[] = TestLogger.getAppLogLines();
        //Assert.assertTrue(logLines.length>1);
    }

    @Test
    public void testSimpleMatch() throws InterruptedException {

        logger.info("testSimpleMatch");

        lmtBuy("C1", SYM_IBM, 100, 10.50);
        lmtSell("C1", SYM_IBM, 100, 10.50);

        showBook(SYM_IBM);

        // check both sides are empty again
        assertBookBidAskEmpty(SYM_IBM);

        // check trade has occurred
        assertTrades("ibm.n,100,10.50");

        String logLines[] = TestLogger.getAppLogLines();
        //Assert.assertTrue(logLines.length >= 0);
    }

    @Test
    public void testMatchPriceImprovement() throws InterruptedException {

        lmtBuy("C1", SYM_IBM, 100, 15.00);
        lmtSell("C1", SYM_IBM, 300, 10.00);

        showBook(SYM_IBM);

        // check quote feed
        assertBook(SYM_IBM, "     |200 x 10",
                "empty|");

        assertTrades("ibm.n,100,12.50");
    }

    @Test
    public void testMatchAndPostTwoSymbols() throws InterruptedException {

        lmtBuy("C1", SYM_IBM, 100, 15.00);
        lmtSell("C1", SYM_IBM, 300, 10.00);

        lmtSell("C1", SYM_AAPL, 250, 10.20);
        lmtBuy("C1", SYM_AAPL, 100, 10.20);

        showBook(SYM_IBM);
        showBook(SYM_AAPL);

        // check quote feed
        assertBook(SYM_IBM, "     |200 x 10.00",
                "empty|");
        assertBook(SYM_AAPL, "     |150 x 10.20",
                "empty|");
        // check trade feed
        assertTrades("ibm.n,100,12.50",
                "aapl.q,100,10.20");
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

        lmtBuy("C1", SYM_IBM, 100, 10.5055667); // decimal price
        lmtSell("C1", SYM_IBM, 100, 11.789); // decimal price

        showBook(sym);

        // construct expected book and verify
        // highest bid price to clients is on top
        assertBook(SYM_IBM, "100", "10.51",
                "100", "11.79");

        showBook(sym);
    }

    /**
     * You have to coordinate buys and sell prices.
     * Otherwise the book will price improve and writing a test is harder
     *
     * @throws InterruptedException
     */
    @Test
    public void testWaveOfBuyFollowedBySellOrders() throws InterruptedException {

        logger.info("Begin Test");

        String sym = "ibm.n";

        int orders_n = 1200;
        double initialPrice = 1.00;
        double delta = 0.01;
        double lastPrice = 0;
        for (int i = 0; i < orders_n; i++) {
            double orderPrice = initialPrice + i * delta;
            lastPrice = orderPrice;
            lmtBuy("C1", SYM_IBM, 100, orderPrice);

        }
        // sells go in the opposite direction, starting from the highest bid
        // bid is how much you are willing to pay for something
        int bla = 0;
        initialPrice = lastPrice;
        for (int i = 0; i < orders_n; i++) {
            double orderPrice = initialPrice - i * delta;
            lmtSell("C1", SYM_IBM, 100, orderPrice);

        }

        logger.info("\n" + engine.getQuoteFeed().getBook(sym));

        // book must be empty
        assertBookBidAskEmpty(SYM_IBM);

        // check trade feed
        initialPrice = 1.00;
        for (int i = 0; i < orders_n; i++) {
            double matchPx = initialPrice + i * delta;
            tradesFeed.add(new DefaultTradeFeedMsg(System.nanoTime(), SYM_IBM, 100, matchPx)); // price is improved for both
        }
        // reverse the trades since they were matched from highest to lower price.
        Collections.reverse(tradesFeed);
        // check trade feed
        assertTrades(tradesFeed, getTradeFeedMsgs(sym));
    }

    @Test
    public void testCancel() throws InterruptedException {

        logger.info("Begin Test");

        lmtBuy("C1", SYM_IBM, 100, 10.50);
        FixOrder sellOrder1 = lmtSell("C1", SYM_IBM, 250, 10.50);
        FixOrder sellOrder2 = lmtSell("C1", SYM_IBM, 300, 10.50);
        FixOrder sellOrder3 = lmtSell("C1", SYM_IBM, 200, 10.70);

        showBook(SYM_IBM);

        assertBook(SYM_IBM, "     |200 x 10.70",
                "     |450 x 10.50",
                "empty|");

        cancelOrder(sellOrder1);

        assertBook(SYM_IBM, "     |200 x 10.70",
                "     |300 x 10.50",
                "empty|");

        cancelOrder(sellOrder2);

        assertBook(SYM_IBM, "     |200 x 10.70",
                "empty|");
        cancelOrder(sellOrder3);

        assertBookBidAskEmpty(SYM_IBM);

        // check trade has occurred
        assertTrades("ibm.n,100,10.50");

        String logLines[] = TestLogger.getAppLogLines();
        //Assert.assertTrue(logLines.length > 1);
    }

    @Test
    public void testAmend() throws InterruptedException {

        logger.info("testAmend");

        lmtBuy("C1", SYM_IBM, 100, 10.50);
        FixOrder sellOrder1 = lmtSell("C1", SYM_IBM, 250, 10.50);
        FixOrder sellOrder2 = lmtSell("C1", SYM_IBM, 300, 10.50);
        FixOrder sellOrder3 = lmtSell("C1", SYM_IBM, 200, 10.70);

        showBook(SYM_IBM);

        assertBook(SYM_IBM, "     |200 x 10.70",
                "     |450 x 10.50",
                "empty|");

        //Amend amend1=amendOrder(sellOrder1,50,sellOrder1.getPrice());


        String logLines[] = TestLogger.getAppLogLines();
        //Assert.assertTrue(logLines.length > 1);
    }

}
