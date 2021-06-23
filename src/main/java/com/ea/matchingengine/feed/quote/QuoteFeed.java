package com.ea.matchingengine.feed.quote;

import com.ea.matchingengine.book.BookKey;

import java.util.List;

/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/
public interface QuoteFeed {

    void reportBookChange(FeedMsgSide bookSide, String symbol, BookKey px_level, int beforeQty, int afterQty);

    List<QuoteMsg> getBids(String symbol);

    List<QuoteMsg> getOffers(String symbol);

    /**
     * Very slow method use for debugging.
     * @param symbol
     * @return
     */
    String getBook(String symbol);
}
