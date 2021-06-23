/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.fix.input;

/**
 * This is not a FIX order.
 * It is an order put on a book,
 * original and currently open quantity.
 */
public interface Order extends Request {

    OrderType getType();

    OrderSide getSide();

    double getPrice();

    int getSize(); // original qty

    int getOpenQty(); // open, unfilled qty.

    void fillQty(int matchQty); // reduce open qty by matchQty
}
