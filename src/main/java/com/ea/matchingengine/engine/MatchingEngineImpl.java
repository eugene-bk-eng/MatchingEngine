/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine.engine;

import com.ea.matchingengine.LoggerNames;
import com.ea.matchingengine.book.Book;
import com.ea.matchingengine.book.BookImpl;
import com.ea.matchingengine.book.order.api.BookCancel;
import com.ea.matchingengine.book.order.impl.BookCancelImpl;
import com.ea.matchingengine.book.order.api.BookOrder;
import com.ea.matchingengine.book.order.impl.BookOrderImpl;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.quote.QuoteFeedImpl;
import com.ea.matchingengine.feed.trade.TradeFeed;
import com.ea.matchingengine.feed.trade.TradeFeedImpl;
import com.ea.matchingengine.fix.client.FixAmend;
import com.ea.matchingengine.fix.client.FixCancel;
import com.ea.matchingengine.fix.client.FixOrder;
import com.ea.matchingengine.fix.client.FixOrderImpl;
import com.ea.matchingengine.fix.client.MsgType;
import com.ea.matchingengine.fix.client.Request;
import com.google.common.base.Preconditions;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

// prepare for conversation with AMM

public class MatchingEngineImpl implements MatchingEngine {

    private final Logger logger = LogManager.getLogger(LoggerNames.getAppLoggerName());

    final Map<String, Book> mapBook = new ConcurrentHashMap();
    final Map<String, BlockingQueue<Request>> mapBookOrdQueue = new ConcurrentHashMap();
    final BlockingQueue<String> timePriority = new LinkedBlockingQueue<>();
    final QuoteFeed quoteFeed;
    final TradeFeed tradeFeed;

    volatile boolean flagDispatchQueue;
    ExecutorService executorService;

    public MatchingEngineImpl(org.apache.commons.configuration2.Configuration config) {

        // set up book feed
        quoteFeed = new QuoteFeedImpl();
        tradeFeed = new TradeFeedImpl();

    }

    @Override
    public void startMatching() {
        logger.info("startMatching");
        initDispatch();
    }

    public void initDispatch() {
        logger.info("initDispatch");
        // set up thread pool
        executorService = Executors.newSingleThreadExecutor();

        // dispatch incoming request to thread pool
        startDispatch();
    }

    /**
     * Starts matching engine processing incoming requests
     */
    void startDispatch() {
        logger.info("startDispatch");
        // set up thread dispatching blocking queue
        flagDispatchQueue = true;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (flagDispatchQueue) {
                        waitAndProcessNextMsg();
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getLocalizedMessage(), e);
                    Thread.currentThread().interrupt(); // does not require declaring exception yet will carry error.
                }
            }
        });
    }


    @Override
    public void waitAndProcessNextMsg() throws InterruptedException {
        logger.debug("processNextQueueMsg");
        // blocking call. will return either a new or existing book for this symbol.
        Request request = mapBookOrdQueue.get(timePriority.take()).take();
        Book book = mapBook.computeIfAbsent(request.getSym(), p -> new BookImpl(request.getSym(), getQuoteFeed(), getTradeFeed()));
        // take a lock on the book object
        synchronized (book) {
            switch (request.getMsgType()) {
                case ORDER:
                    logger.info("RCVD ORDER: " + request);
                    FixOrder order = (FixOrder) request;
                    validate(order);
                    book.match(convertToBookOrder(order));
                    break;
                case CANCEL:
                    logger.info("RCVD CANCEL: " + request);
                    FixCancel cancel = (FixCancel) request;
                    validate(cancel);
                    book.match(convertToBookOrder(cancel));
                    break;
            }
        }
    }

    /**
     * TODO: you should replace client order with something that
     * only belongs to this client.
     *
     * @param order
     * @return
     */
    private BookOrder convertToBookOrder(FixOrder order) {
        BookOrder bookOrder = new BookOrderImpl(order.getClientId(), order.getOrderId(), order.getSym(), order.getSide(), order.getType(), order.getQty(), order.getPrice());
        return bookOrder;
    }

    private BookCancel convertToBookOrder(FixCancel cancel) {
        BookCancel bookCancel = new BookCancelImpl(cancel.getClientId(), cancel.getOrderId(), cancel.getSym());
        return bookCancel;
    }

    // match happens asynchronously. caller returns quickly
    @Override
    public void accept(Request request) {
        // validate order
        validate(request);
        //
        if (request.getMsgType() == MsgType.ORDER) {
            validate((FixOrder) request);
        } else if (request.getMsgType() == MsgType.AMEND) {
            validate((FixAmend) request);
        }
        // normalize price to two/four decimal points based on RegNMS
        request = normalizePriceDecimalPoint(request);
        // add
        Queue<Request> queue = mapBookOrdQueue.computeIfAbsent(request.getSym(), p -> new LinkedBlockingQueue());
        queue.offer(request);
        timePriority.offer(request.getSym()); // to keep track of order of insertion.
    }

    // TODO: slow method, figure out better solution.
    // Price must be normalized because double is used as a key
    // in a sorted book structure. Per stock, prices must be normalizeds to the same
    // decimal, two or 4 places.
    private Request normalizePriceDecimalPoint(Request request) {
        if (request.getMsgType() == MsgType.ORDER) {
            FixOrder order = (FixOrder) request;
            double roundedPrice = BigDecimal.valueOf(order.getPrice()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            FixOrder newOrder = new FixOrderImpl(order, roundedPrice);
            return newOrder;
        } else if (request.getMsgType() == MsgType.AMEND) {
            throw new IllegalStateException();
//            Amend amend = (Amend) request;
//            double roundedPrice = BigDecimal.valueOf(amend.getAmendedPrice()).setScale(
//                    2, RoundingMode.HALF_UP).doubleValue();
//            Amend newAmend=new AmendImpl(
//                    amend.getId(),
//                    amend.getSym(),amend.getType(),
//                    amend.getSide(), amend.getSize(), roundedPrice, amend.getOldPrice());
            //return newAmend;
        }
        return request;
    }

    @Override
    public void shutdown() {
        // TODO: add shutdown routines
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    void validate(Request request) {
        Preconditions.checkArgument(request.getMsgType() != null);
        Preconditions.checkArgument(request.getSym() != null);
        Preconditions.checkArgument(request.getSym() != null);
    }

    void validate(FixOrder order) {
        Preconditions.checkArgument(order.getPrice() > 0, order);
        Preconditions.checkArgument(order.getQty() > 0, order);
        Preconditions.checkArgument(order.getSym().length() > 0, order);
    }

    void validate(FixAmend amend) {
        Preconditions.checkArgument(amend.getAmendedPrice() > 0, amend);
        Preconditions.checkArgument(amend.getSize() > 0, amend);
        Preconditions.checkArgument(amend.getSym().length() > 0, amend);
    }

    @Override
    public QuoteFeed getQuoteFeed() {
        return quoteFeed;
    }

    @Override
    public TradeFeed getTradeFeed() {
        return tradeFeed;
    }

    public static void main(String args[]) throws Exception {
        org.apache.commons.configuration2.Configuration config = new MapConfiguration(new HashMap());
        new MatchingEngineImpl(config);
    }
}
