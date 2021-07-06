/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.client;

public interface Request {

    MsgType getMsgType();

    String getSym();

    String getOrderId();

    String getClientId();
}
