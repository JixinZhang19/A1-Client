package producerConsumer;

import model.SkierTask;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierProducer implements Runnable {

    private final BlockingQueue<SkierTask> queue;

    private final int requestNum;

    private static final Random random = new Random();

    public SkierProducer(BlockingQueue<SkierTask> queue, int requestNum) {
        this.queue = queue;
        this.requestNum = requestNum;
    }

    @Override
    public void run() {
        for (int i = 0; i < requestNum; i++) {
            try {
                queue.put(generateRandomTask());
            } catch (InterruptedException e) {
                System.out.println("[SEVERE] Error putting task into skier queue: " + e.getMessage());
            }
        }
    }

    /**
     * @return SkierTask
     * @Description Create random SkierTask with ThreadLocalRandom
     */
    public SkierTask generateRandomTask() {
        return new SkierTask(
                1,
                "2024",
                String.valueOf(random.nextInt(3) + 1),
                random.nextInt(100000) + 1,
                random.nextInt(360) + 1,
                random.nextInt(40) + 1
        );
    }

}
