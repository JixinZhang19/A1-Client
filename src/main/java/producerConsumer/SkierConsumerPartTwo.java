package producerConsumer;

import api.SkierApi;
import model.FileTask;
import model.LifeRide;
import model.SkierTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-04
 */
public class SkierConsumerPartTwo implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SkierConsumerPartTwo.class.getName());

    private final BlockingQueue<SkierTask> queue;

    private final ConcurrentLinkedQueue<FileTask> fileQueue;

    private final CountDownLatch startLatch;

    private final AtomicInteger successCount;

    private final AtomicInteger failCount;

    private final int POST_REQ_EACH_THREAD;

    private static final SkierApi skierApi = new SkierApi();

    public SkierConsumerPartTwo(BlockingQueue<SkierTask> queue, ConcurrentLinkedQueue<FileTask> fileQueue, CountDownLatch startLatch, AtomicInteger successCount, AtomicInteger failCount, int postReqEachThread) {
        this.queue = queue;
        this.fileQueue = fileQueue;
        this.startLatch = startLatch;
        this.successCount = successCount;
        this.failCount = failCount;
        this.POST_REQ_EACH_THREAD = postReqEachThread;
    }

    @Override
    public void run() {
        try {
            // List<FileTask> fileTaskList = new ArrayList<>();
            for (int k = 0; k < POST_REQ_EACH_THREAD; k++) {
                // Take task from skier queue
                SkierTask task = queue.take();
                Integer resortID = task.getResortID();
                String seasonID = task.getSeasonID();
                String dayID = task.getDayID();
                Integer skierID = task.getSkierID();
                LifeRide lifeRide = new LifeRide(task.getTime(), task.getLiftID());

                // Call SkierApi
                long start = System.currentTimeMillis();
                int code = skierApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                long end = System.currentTimeMillis();

                // fileTaskList.add(new FileTask(start, "POST", end - start, code));
                fileQueue.offer(new FileTask(start, "POST", end - start, code));

                // Check if request successes
                if (code == 201) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }

//            fileExecutor.execute(() -> {
//                for (FileTask fileTask: fileTaskList) {
//                    fileQueue.offer(fileTask);
//                }
//            });
            // Once a thread completes its task, release startLatch
            startLatch.countDown();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error taking task from skier queue: " + e.getMessage(), e);
        } finally {
            // Remove ThreadLocal
            skierApi.close();
        }
    }
}
