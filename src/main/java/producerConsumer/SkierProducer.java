package producerConsumer;

import model.SkierTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierProducer implements Runnable {

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
                // throw new RuntimeException(e);
                System.out.println("InterruptedException");
            }
        }
    }

    public SkierTask generateRandomTask() {
        // 随机生成请求参数和请求体，组装成一个post request task放入队列
//        resortID - between 1 and 10
//        seasonID - 2024
//        dayID - 1
//        skierID - between 1 and 100000
//        time - between 1 and 360
//        liftID - between 1 and 40
        return new SkierTask(
                ThreadLocalRandom.current().nextInt(10) + 1,
                "2024",
                "1",
                ThreadLocalRandom.current().nextInt(100000) + 1,
                ThreadLocalRandom.current().nextInt(360) + 1,
                ThreadLocalRandom.current().nextInt(40) + 1);
    }

}
