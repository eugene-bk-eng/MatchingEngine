package com.ea.matchingengine.book;

import java.util.Objects;

/**
 * Comparable is required such TreeMap can build a tree and order keys
 * <p>
 * Wrapping is helpful such that
 *
 * @author : eugene
 * @created : 6/22/2021, Tuesday
 **/
public class BookKey implements Comparable {
    private final double price;

    public BookKey(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookKey bookKey = (BookKey) o;
        return Double.compare(bookKey.price, price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(price);
    }

    @Override
    public int compareTo(Object o) {
        return Double.compare(price, ((BookKey) o).getPrice());
    }

    @Override
    public String toString() {
        return "BookKey{" +
                "price=" + price +
                '}';
    }
}
