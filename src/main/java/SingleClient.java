import api.SkierApi;
import model.LifeRide;
import model.SkierTask;
import producerConsumer.SkierProducer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 * <p>
 */

public class SingleClient {

    private static final BlockingQueue<SkierTask> queue = new LinkedBlockingQueue<>();

    private static final AtomicInteger successCount = new AtomicInteger(0);

    private static final AtomicInteger failCount = new AtomicInteger(0);

    private static final SkierApi skierApi = new SkierApi();

    private static final int NUM_REQUEST = 10000;

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        // Start 1 skier producer
        (new Thread(new SkierProducer(queue, NUM_REQUEST))).start();

        Thread thread = new Thread(() -> {
            try {
                for (int k = 0; k < NUM_REQUEST; k++) {
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
                // Remove ThreadLocal
                skierApi.close();
            }
        });
        thread.start();

        thread.join();
        long end = System.currentTimeMillis();
        System.out.println("Single Client");
        System.out.println("Number of successful requests: " + successCount.get());
        System.out.println("Number of unsuccessful requests: " + failCount.get());
        System.out.println("Total run time (milliseconds): " + (end - start));
    }

}
