package producerConsumer;

import api.SkierApi;
import model.LifeRide;
import model.SkierTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierConsumerPartOne implements Runnable {

    private final BlockingQueue<SkierTask> queue;

    private final CountDownLatch startLatch;

    private final AtomicInteger successCount;

    private final AtomicInteger failCount;

    private final int POST_REQ_EACH_THREAD;

    private static final SkierApi skierApi = new SkierApi();

    public SkierConsumerPartOne(BlockingQueue<SkierTask> queue, CountDownLatch startLatch, AtomicInteger successCount, AtomicInteger failCount, int postReqNumEachThread) {
        this.queue = queue;
        this.startLatch = startLatch;
        this.successCount = successCount;
        this.failCount = failCount;
        this.POST_REQ_EACH_THREAD = postReqNumEachThread;
    }

    @Override
    public void run() {
        try {
            for (int k = 0; k < POST_REQ_EACH_THREAD; k++) {
                // Take task from skier queue
                SkierTask task = queue.take();
                Integer resortID = task.getResortID();
                String seasonID = task.getSeasonID();
                String dayID = task.getDayID();
                Integer skierID = task.getSkierID();
                LifeRide lifeRide = new LifeRide(task.getTime(), task.getLiftID());

                // Call SkierApi
                int code = skierApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);

                // Check if request successes
                if (code == 201) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[SEVERE] Error taking task from skier queue: " + e.getMessage());
        } finally {
            // Once a thread completes its task, release startLatch
            startLatch.countDown();
            // Remove ThreadLocal
            skierApi.close();
        }
    }

}
