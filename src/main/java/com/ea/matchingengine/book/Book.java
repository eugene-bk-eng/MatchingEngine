/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.book;

import com.ea.matchingengine.book.order.api.BookCancel;
import com.ea.matchingengine.book.order.api.BookOrder;

public interface Book {

    void match(BookOrder order);

    void match(BookCancel cancel);
}
