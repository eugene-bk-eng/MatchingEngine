/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.book;

import com.ea.matchingengine.fix.input.Amend;
import com.ea.matchingengine.fix.input.Cancel;
import com.ea.matchingengine.fix.input.Order;

public interface Book {

    void match(Order order);

    void match(Cancel cancel);

    void match(Amend amend);
}
