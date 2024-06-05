import model.SkierTask;
import producerConsumer.SkierConsumerPartOne;
import producerConsumer.SkierProducer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 * <p>
 * 100 * 1680 threads -> 74953 ms, 30988 33971 ms (x) 连接数有可能超过服务器设置的最大连接数，导致连接北重置
 * 200 * 840 threads -> 58302 ms, 30520 31922 ms (y)
 * 300 * 560 threads -> 60472 ms, 30561 30561 ms
 * 400 * 420 threads -> 77254 ms, 30553 42883 ms (x)
 * 1000 * 168 threads -> 90269 ms, 30757 43433 ms (x)
 */
// todo:
//  done 1) producer-consumer模式：单个producer线程（永远不必等待事件可用，消耗尽可能少的CPU和内存）随机生成post请求参数和内容，多个consumer线程并发消费发起请求
//  done 2) 32个线程 * 每个线程1000个请求 -> 多少个线程 * 每个线程多少个请求 performance最好（只需要保证一共发送200K个请求）+ jmeter测试
//          -> 目前 200 * 840 thread 的performance最好
//  done 3）httpClient 相关，关于连接的关闭策略，如何确保每个线程发送完1000个post请求后（完成任务后）关闭连接；如果同一个线程被复用，连接可以不关闭吗？
//          -> response的关闭（每个请求结束后）；httpClient的关闭 <- threadLocal的remove（每个线程任务完成后）
//  done 4) IOException处理，并在最外层统计成功的请求数和不成功的请求数（原子类count来解决并发问题）
//  5) expect throughput (little's rule) & real throughput
//  done 6) newFixedThreadPool 和 newCachedThreadPool 哪个更好？newFixedThreadPool 的线程个数设为多少？
//          -> 先定 newCachedThreadPool
//  done 7) server线程启用情况和client线程数/httpClient数的关系？server端thread pool如何设置？
//          -> httpClient对应服务端connection，一般一个连接只在一个thread上处理
//          -> maxConnection 1000, maxThreads 500, initThreads 30(or 40), waitQueue 1000
//  done 8) InterruptedException 如何处理，所有出现的异常是否都处理了?
//  done 9) 将sout都改为log, 每个类一个logger
public class MultiClientPartOne {

    private static final Logger LOGGER = Logger.getLogger(MultiClientPartOne.class.getName());

    private static final ExecutorService executorFirst = Executors.newFixedThreadPool(32);

    private static final ExecutorService executorSecond = Executors.newFixedThreadPool(840);

    private static final BlockingQueue<SkierTask> queue = new LinkedBlockingQueue<>();

    private static final CountDownLatch startLatch = new CountDownLatch(1);

    private static final AtomicInteger successCount = new AtomicInteger(0);

    private static final AtomicInteger failCount = new AtomicInteger(0);

    private static final int POST_REQ_EACH_THREAD_FIRST = 1000;

    private static final int THREADS_NUM_FIRST = 32;

    private static final int POST_REQ_EACH_THREAD_SECOND = 200;

    private static final int THREADS_NUM_SECOND = 840;

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        // Start 1 skier producer
        (new Thread(new SkierProducer(queue))).start();

        // Start 32 skier consumers
        for (int i = 0; i < THREADS_NUM_FIRST; i++) {
            executorFirst.execute(new SkierConsumerPartOne(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_FIRST));
        }

        // Wait until one skier consumer complete its task
        startLatch.await();
        int reqStage1 = successCount.get();
        long endStage1 = System.currentTimeMillis();
        System.out.println("Stage1 throughput (requests/second): " + reqStage1 / ((endStage1 - start) / 1000));
        System.out.println("-----------------------------------------------------");
        // Start remaining skier consumers
        for (int i = 0; i < THREADS_NUM_SECOND; i++) {
            executorSecond.execute(new SkierConsumerPartOne(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_SECOND));
        }

        executorFirst.shutdown();
        executorSecond.shutdown();
        // Wait for all tasks in the thread pool to complete executing or wait 2 minute
        if (!executorFirst.awaitTermination(2, TimeUnit.MINUTES) || !executorSecond.awaitTermination(2, TimeUnit.MINUTES)) {
            // Timeout
            LOGGER.log(Level.SEVERE, "Thread pool wait timeout");
        }

        long end = System.currentTimeMillis();
        System.out.println("Stage2 throughput (requests/second): " + (200000 - reqStage1) / ((end - endStage1) / 1000));
        System.out.println("-----------------------------------------------------");
        System.out.println("Number of successful requests: " + successCount.get());
        System.out.println("Number of unsuccessful requests: " + failCount.get());
        System.out.println("Total run time (milliseconds): " + (end - start));
        System.out.println("Total throughput (requests/second): " + 200000 / ((end - start) / 1000));
    }

}
