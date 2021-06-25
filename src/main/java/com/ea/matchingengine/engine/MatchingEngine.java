/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.engine;

import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.trade.TradeFeed;
import com.ea.matchingengine.fix.input.Request;

public interface MatchingEngine {

    void accept(Request order);

    void startMatching();

    void shutdown();

    void processQueue() throws InterruptedException;

    QuoteFeed getQuoteFeed();

    TradeFeed getTradeFeed();
}
