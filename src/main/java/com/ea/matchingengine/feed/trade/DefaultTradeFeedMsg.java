package com.ea.matchingengine.feed.trade;

/**
 * @author : eugene
 * @created : 6/19/2021, Saturday
 **/

import java.util.Objects;

/**
 * * Actual feed message include:
 * * TYPE     Symbol  Side    Qty     Price
 * * N/U/D    IBM     BID     100     10.50
 */
public class DefaultTradeFeedMsg implements TradeMsg {
    final String sym;
    final int size;
    final double price;
    final long timestamp_rcvd;
    final long regularTime;

    public DefaultTradeFeedMsg(long timestamp_rcvd, String sym, int size, double price) {
        this.timestamp_rcvd=timestamp_rcvd;
        this.regularTime = System.currentTimeMillis();
        this.sym = sym;
        this.size = size;
        this.price = price;
    }

    public String getSym() {
        return sym;
    }

    public int getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public long getTime() { return regularTime;  }

    @Override
    public long getHighResTime() { return timestamp_rcvd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultTradeFeedMsg that = (DefaultTradeFeedMsg) o;
        return size == that.size && Math.abs(that.price-price) <= 0.001 && Objects.equals(sym, that.sym);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sym, size, price);
    }

    @Override
    public String toString() {
        return "TradeMsg{" +
                "sym='" + sym + '\'' +
                ", size=" + size +
                ", price=" + price +
                '}';
    }
}
