package com.ea.matchingengine.book.order.api;

import com.ea.matchingengine.book.order.impl.OrderId;
import com.ea.matchingengine.fix.client.OrderSide;
import com.ea.matchingengine.fix.client.OrderType;

/**
 * @author : eugene
 * @created : 7/3/2021, Saturday
 **/
public interface BookOrder {

    OrderId getOrderId();

    String getSym();

    OrderType getType();

    OrderSide getSide();

    double getPrice();

    int getBookQty();

    int consumeQty(int consumeQty); // match quantity, reducing available
}
