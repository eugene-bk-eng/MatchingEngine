package com.ea.matchingengine.feed.quote;

/**
 * @author : eugene
 * @created : 6/19/2021, Saturday
 **/
public interface QuoteMsg {

    long getHighResTime();

    long getTime();

    String getSym();

    int getSize();

    double getPrice();
}
