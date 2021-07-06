/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.client;

import java.util.UUID;

public class FixOrderImpl extends RequestBase implements FixOrder {

    private final OrderType type;
    private final OrderSide side;
    private final double price;
    private final int qty; // original order

    public FixOrderImpl(String clientId, String symbol, OrderType type, OrderSide side, int size, double price) {
        super(clientId, UUID.randomUUID().toString(), symbol, MsgType.ORDER);
        this.type = type;
        this.side = side;
        this.qty = size;
        this.price = price;
    }

    /**
     * clone constructor
     */
    public FixOrderImpl(FixOrder order, double fixedPx) {
        super(order.getClientId(), order.getOrderId(), order.getSym(), order.getMsgType());
        this.type = order.getType();
        this.side = order.getSide();
        this.qty = order.getQty();
        this.price = fixedPx;
    }

    @Override
    public OrderType getType() {
        return type;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public int getQty() {
        return qty;
    }

    @Override
    public OrderSide getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "{" +
                "" + msgType +
                ", " + symbol +
                ", " + type +
                ", " + side +
                ", " + qty +
                ", " + price +
                ", id='" + orderId + '\'' +
                '}';
    }
}
