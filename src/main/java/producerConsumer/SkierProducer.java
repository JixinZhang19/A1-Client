package producerConsumer;

import model.SkierTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierProducer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SkierProducer.class.getName());

    private final BlockingQueue<SkierTask> queue;

    private static final int REQUEST_NUM = 200000;

    public SkierProducer(BlockingQueue<SkierTask> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        for (int i = 0; i < REQUEST_NUM; i++) {
            try {
                queue.put(generateRandomTask());
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Error putting task into skier queue: " + e.getMessage(), e);
            }
        }
    }

    /**
     * @Description Create random SkierTask with ThreadLocalRandom
     * @return SkierTask
     */
    public SkierTask generateRandomTask() {
        return new SkierTask(
                ThreadLocalRandom.current().nextInt(10) + 1,
                "2024",
                "1",
                ThreadLocalRandom.current().nextInt(100000) + 1,
                ThreadLocalRandom.current().nextInt(360) + 1,
                ThreadLocalRandom.current().nextInt(40) + 1
        );
    }

}
