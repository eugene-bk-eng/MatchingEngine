/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine.fix.input;

public interface Amend {
    OrderType getType(); // can change limit to market

    double getPrice();

    int getSize();

    OrderSide getSide(); // can't change typ
}
