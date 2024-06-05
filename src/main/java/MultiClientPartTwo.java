import model.FileTask;
import model.SkierTask;
import producerConsumer.FileConsumer;
import producerConsumer.SkierConsumerPartTwo;
import producerConsumer.SkierProducer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-04
 */
//Number of successful requests: 200000
//Number of unsuccessful requests: 0
//Total run time of completing 200K requests (milliseconds): 57918
//Total throughput (requests/second): 3508
//Total run time of completing writing file (milliseconds): 58395
//Total throughput (requests/second): 3448
// todo: 突然很慢
public class MultiClientPartTwo {

    private static final Logger LOGGER = Logger.getLogger(MultiClientPartTwo.class.getName());

    private static final ExecutorService executor = Executors.newCachedThreadPool();

//    private static final ExecutorService fileExecutor = Executors.newCachedThreadPool();

    private static final BlockingQueue<SkierTask> queue = new LinkedBlockingQueue<>();

    private static final ConcurrentLinkedQueue<FileTask> fileQueue = new ConcurrentLinkedQueue<>();

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
            executor.execute(new SkierConsumerPartTwo(queue, fileQueue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_FIRST));
        }

        // Wait until one skier consumer complete its task
        startLatch.await();
        int reqStage1 = successCount.get();
        long endStage1 = System.currentTimeMillis();
        System.out.println("Stage1 throughput (requests/second): " + reqStage1 / ((endStage1 - start) / 1000));
        System.out.println("-----------------------------------------------------");

        // Start remaining skier consumers
        for (int i = 0; i < THREADS_NUM_SECOND; i++) {
            executor.execute(new SkierConsumerPartTwo(queue, fileQueue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_SECOND));
        }

        executor.shutdown();
        // Wait for all tasks in the thread pool to complete executing or wait 2 minute
        if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
            // Timeout
            LOGGER.log(Level.SEVERE, "Thread pool wait timeout");
        }

        long end = System.currentTimeMillis();
        System.out.println("Stage2 throughput (requests/second): " + (200000 - reqStage1) / ((end - endStage1) / 1000));
        System.out.println("-----------------------------------------------------");
        System.out.println("Number of successful requests: " + successCount.get());
        System.out.println("Number of unsuccessful requests: " + failCount.get());
        System.out.println("Total run time of completing 200K requests (milliseconds): " + (end - start));
        System.out.println("Total throughput (requests/second): " + 200000 / ((end - start) / 1000));

//        fileExecutor.shutdown();
//        // Wait for all tasks in the thread pool to complete executing or wait 2 minute
//        if (!fileExecutor.awaitTermination(2, TimeUnit.MINUTES)) {
//            // Timeout
//            LOGGER.log(Level.SEVERE, "Thread pool (fileExecutor) wait timeout");
//        }

        // 再启动另一个线程，fileConsumer写入csv文件
        Thread thread = new Thread(new FileConsumer(fileQueue));
        thread.start();
        thread.join();

        long end2 = System.currentTimeMillis();
        System.out.println("Total run time of completing writing file (milliseconds): " + (end2 - start));
        System.out.println("Total throughput (requests/second): " + 200000 / ((end2 - start) / 1000));
    }

}
