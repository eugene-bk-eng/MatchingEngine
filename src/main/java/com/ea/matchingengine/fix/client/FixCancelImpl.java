/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.client;

import java.util.UUID;

public class FixCancelImpl extends RequestBase implements FixCancel {

    protected final String cancelId;

    public FixCancelImpl(String clientId, String orderId, String symbol) {
        super(clientId, orderId, symbol, MsgType.CANCEL);
        this.cancelId = UUID.randomUUID().toString();
    }
}
