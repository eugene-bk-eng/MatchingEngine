package com.ea.matchingengine.book.model;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * @author : eugene
 * @created : 7/5/2021, Monday
 **/

public class OrderId {
    private final String clientId;
    private final String orderId;

    public OrderId(String clientId, String orderId) {
        Preconditions.checkNotNull(clientId);
        Preconditions.checkNotNull(orderId);
        this.clientId = clientId;
        this.orderId = orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOrderId() {
        return orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId1 = (OrderId) o;
        return Objects.equals(clientId, orderId1.clientId) && Objects.equals(orderId, orderId1.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, orderId);
    }
}
