package com.ea.matchingengine.feed.quote;

import java.util.Objects;

/**
 * @author : eugene
 * @created : 6/19/2021, Saturday
 **/
public class DefaultQuoteMsg implements QuoteMsg {
    String symbol;
    double price;
    int qty;
    final long timestamp_rcvd;
    final long regularTime;

    public DefaultQuoteMsg(long timestamp_rcvd, String symbol, int qty, double price) {
        this.timestamp_rcvd=timestamp_rcvd;
        this.regularTime = System.currentTimeMillis();
        this.symbol = symbol;
        this.qty = qty;
        this.price = price;
    }

    @Override
    public String getSym() {
        return symbol;
    }

    @Override
    public long getTime() { return regularTime;  }

    @Override
    public long getHighResTime() { return timestamp_rcvd; }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public int getSize() {
        return qty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteMsg that = (QuoteMsg) o;
        return Double.compare(that.getPrice(), price) == 0 && qty == that.getSize() && Objects.equals(symbol, that.getSym());
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, price, qty);
    }
}
