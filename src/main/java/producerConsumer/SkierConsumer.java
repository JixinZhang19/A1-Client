package producerConsumer;

import api.SkiersApi;
import model.LifeRide;
import model.SkierTask;

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
    private static final SkiersApi skiersApi = new SkiersApi();

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

                int code = skiersApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
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
        }
    }

}
