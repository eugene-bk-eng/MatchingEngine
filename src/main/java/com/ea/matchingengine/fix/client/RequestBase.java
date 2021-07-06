/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.client;

public class RequestBase implements Request {

    protected final String clientId;
    protected final MsgType msgType;
    protected final String orderId;
    protected final String symbol;

    public RequestBase(String clientId, String orderId, String symbol, MsgType msgType) {
        this.clientId = clientId;
        this.orderId = orderId;
        this.msgType = msgType;
        this.symbol = symbol;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public MsgType getMsgType() {
        return msgType;
    }

    @Override
    public String getSym() {
        return symbol;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

}