package com.ea.matchingengine.book;

import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.trade.TradeFeed;

/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
public abstract class AbstractBook implements Book {

    public final String bookSymbol;
    public final QuoteFeed quoteFeed;
    public final TradeFeed tradeFeed;

    protected AbstractBook(String bookSymbol, QuoteFeed quoteFeed, TradeFeed tradeFeed) {
        this.bookSymbol = bookSymbol;
        this.quoteFeed = quoteFeed;
        this.tradeFeed = tradeFeed;
    }
}
