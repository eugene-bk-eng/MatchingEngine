package com.ea.matchingengine.book.model;

/**
 * @author : eugene
 * @created : 7/5/2021, Monday
 **/
public class BookCancelImpl implements BookCancel {

    private OrderId orderId;
    private String sym;

    public BookCancelImpl(String clientId, String orderId, String sym) {
        this.orderId = new OrderId(clientId,orderId);
        this.sym = sym;
    }

    @Override
    public String getSym() {
        return sym;
    }

    @Override
    public OrderId getOrderId() {
        return orderId;
    }
}
