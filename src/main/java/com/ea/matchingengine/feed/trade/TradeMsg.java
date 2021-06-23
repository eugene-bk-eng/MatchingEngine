package com.ea.matchingengine.feed.trade;

/**
 * @author : eugene
 * @created : 6/19/2021, Saturday
 **/
public interface TradeMsg {

    long getHighResTime();

    long getTime();

    String getSym();

    int getSize();

    double getPrice();
}
