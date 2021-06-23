/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.input;

public interface Request {
    MsgType getMsgType();

    String getSym();

    String getId();

    void setTimestampRcvd(long value);
    long getTimestampRcvd();
}
