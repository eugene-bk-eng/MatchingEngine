/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.engine;

import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.trade.TradeFeed;
import com.ea.matchingengine.fix.client.Request;

public interface MatchingEngine {

    /**
     * Non blocking call. places request on the queue
     *
     * @param order
     */
    void accept(Request order);

    void startMatching();

    void shutdown();

    /**
     * Blocking call if there is nothing to take.
     * internal threads are used to dispatch
     *
     * @throws InterruptedException
     */
    void waitAndProcessNextMsg() throws InterruptedException;

    QuoteFeed getQuoteFeed();

    TradeFeed getTradeFeed();
}
