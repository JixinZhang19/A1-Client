package producerConsumer;

import api.SkierApi;
import model.LifeRide;
import model.SkierTask;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

    private static final int TIMEOUT = 198; // (ms), set to P95 response time
    private final EventCountCircuitBreaker circuitBreaker;

    public SkierConsumerPartOne(BlockingQueue<SkierTask> queue, CountDownLatch startLatch, AtomicInteger successCount, AtomicInteger failCount, int postReqNumEachThread) {
        this.queue = queue;
        this.startLatch = startLatch;
        this.successCount = successCount;
        this.failCount = failCount;
        this.POST_REQ_EACH_THREAD = postReqNumEachThread;
        // If 4 events occur within 2 seconds, the circuit breaker opens
        // Skip requests for 2 seconds after breaker opens, then breaker turns half-on
        // If 2 events occur within 2 seconds of half-on, breaker opens, otherwise breaker closes
        this.circuitBreaker = new EventCountCircuitBreaker(
                4,
                2,
                TimeUnit.SECONDS,
                2,
                2,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        try {
            for (int k = 0; k < POST_REQ_EACH_THREAD; k++) {
                // Check if circuit breaker is open (checkState() returns false)
                if (!circuitBreaker.checkState()) {
                    k--;
                    continue;
                }

                // Take task from skier queue
                SkierTask task = queue.take();
                Integer resortID = task.getResortID();
                String seasonID = task.getSeasonID();
                String dayID = task.getDayID();
                Integer skierID = task.getSkierID();
                LifeRide lifeRide = new LifeRide(task.getTime(), task.getLiftID());

                // Call SkierApi
                long startTime = System.currentTimeMillis();
                int code = skierApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                long duration = System.currentTimeMillis() - startTime;

                // Check if response time is more than P95 response time
                if (duration > TIMEOUT) {
                    boolean state = circuitBreaker.incrementAndCheckState();
                    if (!state) {
                        System.out.println("Thread: " + Thread.currentThread().getId() + ": open circuit breaker.");
                    }
                }

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
