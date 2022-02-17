package com.ea.matchingengine.book.order.api;

import com.ea.matchingengine.book.order.impl.OrderId;

/**
 * @author : eugene
 * @created : 7/5/2021, Monday
 **/
public interface BookCancel {

    String getSym();

    OrderId getOrderId();
}
