/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.client;

/**
 * This is not a FIX order.
 * It is an order put on a book,
 * original and currently open quantity.
 */
public interface FixOrder extends Request {

    OrderType getType();

    OrderSide getSide();

    double getPrice();

    int getQty(); // original qty
}
