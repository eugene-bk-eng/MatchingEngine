/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine.fix.client;

/**
 * Can only change price or quantity
 * <p>
 * Quantity increase unlimited
 * <p>
 * Quantity decrease goes down to whatever quantity is still open on the original.
 */
public interface FixAmend extends Request {

    String getOriginalOrderId();

    OrderType getType();

    OrderSide getSide();

    double getAmendedPrice();

    double getOldPrice();

    int getSize(); // original qty
}
