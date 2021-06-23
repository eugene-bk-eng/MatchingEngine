/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.input;

public class CancelImpl extends RequestBase implements Cancel {
    MsgType msgType;
    String orderId;
    String symbol;

    public CancelImpl(String orderId, String symbol) {
        super(orderId, symbol, MsgType.CANCEL);
    }
}
