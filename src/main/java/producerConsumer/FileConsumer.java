package producerConsumer;

import model.FileTask;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-04
 */
public class FileConsumer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SkierConsumerPartOne.class.getName());

    private final ConcurrentLinkedQueue<FileTask> fileQueue;

    public FileConsumer(ConcurrentLinkedQueue<FileTask> fileQueue) {
        this.fileQueue = fileQueue;
    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"))) {
            // 写入 CSV 文件的标题行
            writer.write("StartTime,RequestType,Latency(ms),ResponseCode\n");
            // 从队列中逐个取出 FileTask，并写入 CSV 文件
            FileTask fileTask;
            for(fileTask = fileQueue.poll(); fileTask != null; fileTask = fileQueue.poll()) {
                writer.write(String.format("%d,%s,%d,%d\n",
                        fileTask.getStartTime(),
                        fileTask.getRequestType(),
                        fileTask.getLatency(),
                        fileTask.getResponseCode()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing to file: " + e.getMessage(), e);
        }
    }
}
