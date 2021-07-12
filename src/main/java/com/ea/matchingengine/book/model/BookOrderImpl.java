package com.ea.matchingengine.book.model;

import com.ea.matchingengine.fix.client.OrderSide;
import com.ea.matchingengine.fix.client.OrderType;
import com.google.common.base.Preconditions;

/**
 * @author : eugene
 * @created : 7/3/2021, Saturday
 **/
public class BookOrderImpl implements BookOrder {

    private final String sym;
    private OrderId orderId;
    private final OrderType orderType;
    private final OrderSide orderSide;
    private int qty;
    private final double px;

    public BookOrderImpl(String clientId, String orderId, String sym, OrderSide orderSide, OrderType orderType, int qty, double px) {
        this.orderId = new OrderId(clientId, orderId);
        this.sym = sym;
        this.orderSide = orderSide;
        this.orderType = orderType;
        this.qty = qty;
        this.px = px;
    }

    @Override
    public String getSym() {
        return sym;
    }

    @Override
    public OrderId getOrderId() {
        return orderId;
    }

    @Override
    public OrderSide getSide() {
        return orderSide;
    }

    @Override
    public OrderType getType() {
        return orderType;
    }

    @Override
    public int getBookQty() {
        return qty;
    }

    // match quantity, reducing available
    @Override
    public int consumeQty(int consumeQty) {
        Preconditions.checkArgument((qty - consumeQty) >= 0);
        qty = qty - consumeQty;
        return qty;
    }

    @Override
    public double getPrice() {
        return px;
    }

    @Override
    public String toString() {
        return "BookOrderImpl{" +
                "sym='" + sym + '\'' +
                ", orderId='" + orderId + '\'' +
                ", qty=" + qty +
                ", px=" + px +
                '}';
    }
}
