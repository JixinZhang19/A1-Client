package producerConsumer;

import api.SkierApi;
import model.LifeRide;
import model.SkierTask;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierConsumer implements Runnable {



    private final BlockingQueue<SkierTask> queue;
    private final CountDownLatch startLatch;
    private final AtomicInteger successCount;
    private final AtomicInteger failCount;
    private final int POST_REQ_EACH_THREAD;
    private static final SkierApi skierApi = new SkierApi();

    public SkierConsumer(BlockingQueue<SkierTask> queue, CountDownLatch startLatch, AtomicInteger successCount, AtomicInteger failCount, int postReqNumEachThread) {
        this.queue = queue;
        this.startLatch = startLatch;
        this.successCount = successCount;
        this.failCount = failCount;
        this.POST_REQ_EACH_THREAD = postReqNumEachThread;
    }

    @Override
    public void run() {
        try {
            // 在每个线程中调用 1000 次 api.SkiersApi 的 POST 方法
            for (int k = 0; k < POST_REQ_EACH_THREAD; k++) {

                SkierTask task = queue.take();
                Integer resortID = task.getResortID();
                String seasonID = task.getSeasonID();
                String dayID = task.getDayID();
                Integer skierID = task.getSkierID();
                LifeRide lifeRide = new LifeRide(task.getTime(), task.getLiftID());
                // long start = System.currentTimeMillis();
                int code = skierApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                // long end = System.currentTimeMillis();
                // todo: 将 start, 请求类型(POST), latency(start - end), code 写入CSV中
                //  -> method 1:  将当前线程所有请求写入一个StringBuilder，在当前线程任务全部结束后，再将StringBuilder中的记录用另外的线程池中的线程写入CSV文件中
                //  -> method 2: 作为生产者写入一个queue(ConcurrentLinkedQueue)，然后消费者从队列中慢慢取就行，不计入post的时间
                if (code == 201) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }
            // 一旦任何一个线程完成，就释放 startLatch
            startLatch.countDown();

        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
            System.out.println("InterruptedException");
        } finally {
            try {
                // remove thread local
                skierApi.close();
            } catch (IOException e) {
                System.out.println("IOException : error closing ThreadLocal");
            }
        }
    }

}
