package com.ea.matchingengine.feed.quote;

/**
 * @author : eugene
 * @created : 6/19/2021, Saturday
 **/
public enum FeedMsgSide {
    BID("BID"),
    OFFER("ASK");

    private String value;

    private FeedMsgSide(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public FeedMsgSide flip() {
        if (this == BID) {
            return OFFER;
        } else if (this == OFFER) {
            return BID;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
