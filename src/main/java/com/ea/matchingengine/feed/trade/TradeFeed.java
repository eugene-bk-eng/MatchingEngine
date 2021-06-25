package com.ea.matchingengine.feed.trade;


import java.util.List;

/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 *
 * TRADE FEED IS SIMPLY THAT, ALL MESSAGES ARE NEW
 *
 * IN THEORY, WE CAN ADD TRADE BUST AND PRICE CORRECTION. THAT'S IN
 * ADDITION TO FIX MESSAGE THAT YOU MUST SEND TO CLIENTS.
 **/
public interface TradeFeed {

    void reportTrade(String symbol, int qtyMatch, double pxMatch);

    List<TradeMsg> getLastTrades();

    List<TradeMsg> getLastTrades(String symbol);
}
