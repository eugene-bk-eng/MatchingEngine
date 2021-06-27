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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feed is supposed to publish to some messaging bus asynchronously via another thread(s)
 */

public class TradeFeedImpl extends AbstractTradeFeed {

    private static final Logger logger = LoggerFactory.getLogger(TradeFeedImpl.class);
    private final List<TradeMsg> listTrades = new LinkedList<>();

    @Override
    public void reportTrade(String symbol, int qtyMatch, double pxMatch) {
        logger.info( String.format("MATCHED TRADE %s, qty:%s, px: %s", symbol, qtyMatch, pxMatch));

        // TODO: build overfill policy
        listTrades.add(new DefaultTradeFeedMsg(System.nanoTime(), symbol, qtyMatch, pxMatch));
    }

    @Override
    public List<TradeMsg> getLastTrades() {
        return Collections.unmodifiableList(listTrades);
    }

    @Override
    public List<TradeMsg> getLastTrades(String symbol) {
        return listTrades.stream().filter(s->s.getSym().equals(symbol)).collect(Collectors.toList());
    }
}
