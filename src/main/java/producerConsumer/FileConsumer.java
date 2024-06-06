package producerConsumer;

import model.FileTask;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-04
 */
public class FileConsumer implements Runnable {

    private final ConcurrentLinkedQueue<FileTask> fileQueue;

    public FileConsumer(ConcurrentLinkedQueue<FileTask> fileQueue) {
        this.fileQueue = fileQueue;
    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"))) {
            // Write the header line of the CSV file
            writer.write("StartTime,RequestType,Latency(ms),ResponseCode\n");
            // Poll FileTask from file queue and write it to the CSV file
            FileTask fileTask;
            for(fileTask = fileQueue.poll(); fileTask != null; fileTask = fileQueue.poll()) {
                writer.write(String.format("%d,%s,%d,%d\n",
                        fileTask.getStartTime(),
                        fileTask.getRequestType(),
                        fileTask.getLatency(),
                        fileTask.getResponseCode()));
            }
        } catch (IOException e) {
            System.out.println("[SEVERE] Error writing to file: " + e.getMessage());
        }
    }
}
