/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
package com.ea.matchingengine.book;

import com.ea.matchingengine.book.model.BookCancel;
import com.ea.matchingengine.book.model.BookOrder;

public interface Book {

    void match(BookOrder order);

    void match(BookCancel cancel);
}
