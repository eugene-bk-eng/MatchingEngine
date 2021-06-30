/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.input;

import java.util.UUID;

public class CancelImpl extends RequestBase implements Cancel {

    final String cancelId;

    public CancelImpl(String orderId, String symbol) {
        super(orderId, symbol, MsgType.CANCEL);
        this.cancelId=UUID.randomUUID().toString();
    }
}
