/**
 * @author : eugene
 * @created : 6/18/2021, Friday
 **/

package com.ea.matchingengine.engine;

import com.ea.matchingengine.book.Book;
import com.ea.matchingengine.book.BookImpl;
import com.ea.matchingengine.feed.quote.QuoteFeed;
import com.ea.matchingengine.feed.quote.QuoteFeedImpl;
import com.ea.matchingengine.feed.trade.TradeFeed;
import com.ea.matchingengine.feed.trade.TradeFeedImpl;
import com.ea.matchingengine.fix.input.Amend;
import com.ea.matchingengine.fix.input.Cancel;
import com.ea.matchingengine.fix.input.MsgType;
import com.ea.matchingengine.fix.input.Order;
import com.ea.matchingengine.fix.input.OrderImpl;
import com.ea.matchingengine.fix.input.Request;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

// prepare for conversation with AMM

public class MatchingEngineImpl implements MatchingEngine {

    private static final Logger logger = LogManager.getLogger(MatchingEngineImpl.class);

    final Map<String, Book> mapBook = new ConcurrentHashMap();
    final Map<String, BlockingQueue<Request>> mapBookOrdQueue = new ConcurrentHashMap();
    final BlockingQueue<String> timePriority = new LinkedBlockingQueue<>();
    final QuoteFeed quoteFeed;
    final TradeFeed tradeFeed;

    volatile boolean flagDispatchQueue;
    ExecutorService executorService;

    public MatchingEngineImpl() {

        // set up book feed
        quoteFeed = new QuoteFeedImpl();
        tradeFeed = new TradeFeedImpl();

        initDispatch();
    }

    public void initDispatch() {
        // set up thread pool
        executorService = Executors.newSingleThreadExecutor();

        // dispatch incoming request to thread pool
        startDispatch();
    }

    /**
     * Starts matching engine processing incoming requests
     */
    void startDispatch() {
        // set up thread dispatching blocking queue
        flagDispatchQueue = true;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (flagDispatchQueue) {
                        processQueue();
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        });
    }

    @Override
    public void processQueue() throws InterruptedException {
        // blocking call. will return either a new or existing book for this symbol.
        Request request = mapBookOrdQueue.get(timePriority.take()).take();
        Book book = mapBook.computeIfAbsent(request.getSym(), p -> new BookImpl(request.getSym(), getQuoteFeed(), getTradeFeed()));
        // take a lock on the book object
        synchronized (book) {
            switch (request.getMsgType()) {
                case ORDER:
                    logger.info("RCVD " + request);
                    Order order = (Order) request;
                    validate(order);
                    book.match(order);
                    break;
                case CANCEL:
                    book.match((Cancel) request);
                    break;
                case AMEND:
                    book.match((Amend) request);
                    break;
            }
        }
    }


    // match happens asynchronously. caller returns quickly
    @Override
    public void accept(Request request) {
        // validate order
        validate(request);
        //
        if( request.getMsgType()==MsgType.ORDER ) {
            validate((Order)request);
        }
        // normalize price to two/four decimal points based on RegNMS
        request=normalizePriceDecimalPoint(request);
        // add
        Queue<Request> queue = mapBookOrdQueue.computeIfAbsent(request.getSym(), p -> new LinkedBlockingQueue());
        queue.offer(request);
        timePriority.offer(request.getSym()); // to keep track of order of insertion.
    }

    // TODO: slow method, figure out better solution.
    private Request normalizePriceDecimalPoint(Request request) {
        if (request.getMsgType() == MsgType.ORDER ||
                request.getMsgType() == MsgType.AMEND) {
            Order order = (Order) request;
            double roundedPrice = BigDecimal.valueOf(order.getPrice()).setScale(
                    2, RoundingMode.HALF_UP).doubleValue();
            Order newOrder=new OrderImpl(
                    order.getSym(),order.getType(),
                    order.getSide(), order.getSize(), roundedPrice);
            return newOrder;
        }
        return request;
    }

    @Override
    public void shutdown() {
    }

    void validate(Request request) {
        Preconditions.checkArgument(request.getMsgType() != null);
        Preconditions.checkArgument(request.getSym() != null);
        Preconditions.checkArgument(request.getSym() != null);
    }

    void validate(Order order) {
        Preconditions.checkArgument(order.getPrice() > 0, order);
        Preconditions.checkArgument(order.getSize() > 0, order);
        Preconditions.checkArgument(order.getSym().length()>0, order);
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
        new MatchingEngineImpl();
    }
}
