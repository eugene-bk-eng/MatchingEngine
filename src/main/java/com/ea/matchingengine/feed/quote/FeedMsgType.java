package com.ea.matchingengine.feed.quote;

/**
 * @author : eugene
 * @created : 6/19/2021, Saturday
 * <p>
 * Quote feed support NEW, UPDATE AND DELETE LEVEL.
 **/
public enum FeedMsgType {
    NEW("N"),
    UPDATE("U"),
    DELETE("D");

    private String value;

    private FeedMsgType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
