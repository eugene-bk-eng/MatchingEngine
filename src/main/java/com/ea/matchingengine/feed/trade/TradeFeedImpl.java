package com.ea.matchingengine.feed.trade;

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

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feed is supposed to publish to some messaging bus asynchronously via another thread(s)
 */

public class TradeFeedImpl extends AbstractTradeFeed {

    private static final Logger logger = LogManager.getLogger(TradeFeedImpl.class);
    private final Map<String, List<TradeMsg>> mapTrades = new HashMap();

    @Override
    public void reportTrade(String symbol, int qtyMatch, double pxMatch) {
        logger.log(Level.INFO, String.format("MATCHED TRADE %s, qty:%s, px: %s", symbol, qtyMatch, pxMatch));

        // TODO: build overfill policy
        mapTrades.computeIfAbsent(symbol, p -> Lists.newArrayList()).add(new DefaultTradeFeedMsg(System.nanoTime(), symbol, qtyMatch, pxMatch));
    }

    @Override
    public List<TradeMsg> getLastTrades(String symbol) {
        return mapTrades.computeIfAbsent(symbol, p -> Lists.newArrayList());
    }
}