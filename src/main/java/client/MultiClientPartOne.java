package client;

import model.SkierTask;
import producerConsumer.SkierConsumerPartOne;
import producerConsumer.SkierProducer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class MultiClientPartOne {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static final BlockingQueue<SkierTask> queue = new LinkedBlockingQueue<>();

    private static final CountDownLatch startLatch = new CountDownLatch(1);

    private static final AtomicInteger successCount = new AtomicInteger(0);

    private static final AtomicInteger failCount = new AtomicInteger(0);

    private static final int NUM_REQUEST = 200000;

    // todo: tuning thread number
    private static final int POST_REQ_EACH_THREAD_FIRST = 300;

    private static final int THREADS_NUM_FIRST = 500;

    private static final int POST_REQ_EACH_THREAD_SECOND = 500;

    private static final int THREADS_NUM_SECOND = 100;

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        // Start 1 skier producer
        (new Thread(new SkierProducer(queue, NUM_REQUEST))).start();

        // Start 32 skier consumers
        for (int i = 0; i < THREADS_NUM_FIRST; i++) {
            executor.execute(new SkierConsumerPartOne(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_FIRST));
        }

        // Wait until one skier consumer complete its task
        startLatch.await();
        int reqStage1 = successCount.get() + failCount.get();
        long endStage1 = System.currentTimeMillis();
        double throughputStage1 = ((double) reqStage1 / (endStage1 - start)) * 1000;
        System.out.println("Multi-thread Client (Part One)");
        System.out.println("-------------------- Stage 1 --------------------");
        System.out.println("Start threads: " + THREADS_NUM_FIRST);
        System.out.println("Number of requests: " + reqStage1);
        System.out.println("Run time (milliseconds): " + (endStage1 - start));
        System.out.println("Throughput (requests/second): " + throughputStage1);
        // Start remaining skier consumers
        for (int i = 0; i < THREADS_NUM_SECOND; i++) {
            executor.execute(new SkierConsumerPartOne(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_SECOND));
        }

        executor.shutdown();
        // Wait for all tasks in the thread pool to complete executing or wait 2 minute
        if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
            // Timeout
            System.out.println("[SEVERE] Thread pool wait timeout");
            executor.shutdownNow();
        }

        long end = System.currentTimeMillis();
        double throughputStage2 = ((double) (NUM_REQUEST - reqStage1) / (end - endStage1)) * 1000;
        double throughput = ((double) NUM_REQUEST / (end - start)) * 1000;
        System.out.println("--------------------- Stage 2 --------------------");
        System.out.println("Start threads: " + THREADS_NUM_SECOND);
        System.out.println("Number of requests: " + (NUM_REQUEST - reqStage1));
        System.out.println("Run time (milliseconds): " + (end - endStage1));
        System.out.println("Throughput (requests/second): " + throughputStage2);
        System.out.println("---------------------  Total  --------------------");
        System.out.println("Number of successful requests: " + successCount.get());
        System.out.println("Number of unsuccessful requests: " + failCount.get());
        System.out.println("Run time (milliseconds): " + (end - start));
        System.out.println("Throughput (requests/second): " + throughput);
    }

}
