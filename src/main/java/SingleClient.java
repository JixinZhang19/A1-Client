import api.SkierApi;
import model.LifeRide;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 * <p>
 * 288435 ms
 */

// todo: 太慢了，server启用的thread似乎很少，为什么？
public class SingleClient {

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        Integer resortID = 12;
        String seasonID = "2019";
        String dayID = "1";
        Integer skierID = 19;
        LifeRide lifeRide = new LifeRide(111, 222);

        SkierApi skierApi = new SkierApi();
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                int code = skierApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                if (code == 201) {
                    successCount.getAndIncrement();
                } else {
                    failCount.getAndIncrement();
                }
            }
        });

        thread.start();
        thread.join(); // 等待线程完成
        try {
            // remove thread local
            skierApi.close();
        } catch (IOException e) {
            System.out.println("IOException : error closing ThreadLocal");
        }

        long end = System.currentTimeMillis();
        System.out.println("time(ms): " + (end - start) + " success req: " + successCount.get() + " fail req: " + failCount.get());
    }


}
