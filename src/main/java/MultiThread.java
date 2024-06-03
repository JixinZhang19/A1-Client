import model.SkierTask;
import producerConsumer.SkierConsumer;
import producerConsumer.SkierProducer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class MultiThread {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final BlockingQueue<SkierTask> queue = new LinkedBlockingQueue<>();
    private static final CountDownLatch startLatch = new CountDownLatch(1);
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    private static final int POST_REQ_EACH_THREAD_FIRST = 1000;
    private static final int THREADS_NUM_FIRST = 32;
    private static final int POST_REQ_EACH_THREAD_SECOND = 2000;
    private static final int THREADS_NUM_SECOND = 84;


    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        // start 1 producer
        (new Thread(new SkierProducer(queue))).start();

        // start 32 consumers
        for (int i = 0; i < THREADS_NUM_FIRST; i++) {
            executor.execute(new SkierConsumer(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_FIRST));
        }

        startLatch.await();

        for (int i = 0; i < THREADS_NUM_SECOND; i++) {
            executor.execute(new SkierConsumer(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_SECOND));
        }

        executor.shutdown();
        // 等待线程池中所有任务执行完毕或者等待1分钟
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            // 如果超时，则输出提示信息
            System.out.println("线程池等待超时，可能有任务未完成");
        }

        long end = System.currentTimeMillis();
        System.out.println("time(ms): " + (end - start) + " success req: " + successCount.get() + " fail req: " + failCount.get());
    }

}
