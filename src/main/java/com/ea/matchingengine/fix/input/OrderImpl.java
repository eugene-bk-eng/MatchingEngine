/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.input;

import java.util.UUID;

public class OrderImpl extends RequestBase implements Order {
    OrderType type;
    OrderSide side;
    double price;
    int size; // original order
    int openQty; // unfilled;

    public OrderImpl(String symbol, OrderType type, OrderSide side, int size, double price) {
        super(UUID.randomUUID().toString(), symbol, MsgType.ORDER);
        this.type = type;
        this.side = side;
        this.openQty = this.size = size;
        this.price = price;
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
    public int getSize() {
        return size;
    }

    @Override
    public OrderSide getSide() {
        return side;
    }

    @Override
    public int getOpenQty() {
        return openQty;
    }

    @Override
    public void fillQty(int matchQty) {
        openQty -= matchQty;
    }

    @Override
    public String toString() {
        return "{" +
                "" + msgType +
                ", " + symbol +
                ", " + type +
                ", " + side +
                ", " + size +
                ", " + price +
                ", openqty=" + openQty +
                ", id='" + orderId + '\'' +
                '}';
    }
}
