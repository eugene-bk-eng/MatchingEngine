package com.ea.matchingengine.feed.quote;

import com.ea.matchingengine.book.BookKey;
import com.google.common.collect.Lists;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : eugene
 * @created : 6/22/2021, Tuesday
 **/
public class BookPrinter {

    public String getBook(String symbol, Map<String, Map<BookKey, QuoteMsg>> bids, Map<String, Map<BookKey, QuoteMsg>> offers) {

        List<BookKey> nbids = Lists.newArrayList(bids.computeIfAbsent(symbol, p -> new HashMap()).keySet());
        List<BookKey> noffers = Lists.newArrayList(offers.computeIfAbsent(symbol, p -> new HashMap()).keySet());
        Collections.sort(nbids, Comparator.reverseOrder());   // first or best is highest price for bid
        Collections.sort(noffers, Comparator.reverseOrder()); // first or best is lowest offer for ask

        StringBuilder sb = new StringBuilder();
        sb.append("\n" + rightPad(".................. " + "BOOK: " + symbol + " ..................", 50) + "\n");
        sb.append(rightPad("-------- " + "BID" + "(" + nbids.size() + ")" + " --------", 25));
        sb.append("|");
        sb.append(leftPad("-------- " + "ASK" + "(" + noffers.size() + ")" + " ---------", 25) + "\n");


        // ask side
        for (int i = 0; i < noffers.size(); i++) {
            sb.append(rightPad("", 25));
            sb.append("|");
            BookKey px = noffers.get(i);
            QuoteMsg record = offers.get(symbol).get(px);
            LocalTime localTime = Instant.ofEpochMilli(record.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
            sb.append(leftPad(rightPad(localTime.toString(), 12) + " " +  rightPad(Integer.toString(record.getSize()), 4) + " x " + rightPad(Double.toString(record.getPrice()), 4), 25));
            sb.append("\n");
        }
        for (int i = 0; i < nbids.size(); i++) {
            BookKey px = nbids.get(i);
            QuoteMsg record = bids.get(symbol).get(px);
            LocalTime localTime = Instant.ofEpochMilli(record.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
            sb.append(leftPad(rightPad(localTime.toString(), 12) + " " + rightPad(Integer.toString(record.getSize()), 4) + " x " + rightPad(Double.toString(record.getPrice()), 4), 25));
            sb.append("|");
            sb.append(leftPad("", 25));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String leftPad(String value, int width) {
        StringBuilder sb = new StringBuilder();
        if (value.length() > width) {
            sb.append(value);
        } else {
            int r = width - value.length();
            sb.append(value);
            sb.append(" ".repeat(r));
        }
        return sb.toString();
    }

    private String rightPad(String value, int width) {
        StringBuilder sb = new StringBuilder();
        if (value.length() > width) {
            sb.append(value);
        } else {
            int r = width - value.length();
            sb.append(" ".repeat(r));
            sb.append(value);
        }
        return sb.toString();
    }

}
