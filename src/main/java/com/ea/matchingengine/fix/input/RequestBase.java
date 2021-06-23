/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.input;

public class RequestBase implements Request {
    MsgType msgType;
    String orderId;
    String symbol;
    long timestamp_rcvd;

    public RequestBase(String orderId, String symbol, MsgType msgType) {
        this.orderId = orderId;
        this.msgType = msgType;
        this.symbol = symbol;
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
    public String getId() {
        return orderId;
    }

    @Override
    public void setTimestampRcvd(long value) {
        timestamp_rcvd=value;
    }

    @Override
    public long getTimestampRcvd() {
        return timestamp_rcvd;
    }

}