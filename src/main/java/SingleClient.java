import api.SkierApi;
import model.LifeRide;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 * <p>
 * 1000 requests: 34976 ms -> latency: 34.976 ms/req -> 32 / 34.976 = 950 req/s
 * 200 requests: 7457 ms -> latency: 37.285 ms/req -> 500 / 37.285 = 13410
 */

// todo: server启用的thread似乎很少？1/(time/req)？

public class SingleClient {

    private static final AtomicInteger successCount = new AtomicInteger(0);

    private static final AtomicInteger failCount = new AtomicInteger(0);

    private static final SkierApi skierApi = new SkierApi();

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        Integer resortID = 12;
        String seasonID = "2019";
        String dayID = "1";
        Integer skierID = 19;
        LifeRide lifeRide = new LifeRide(111, 222);

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 200; i++) {
                int code = skierApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                if (code == 201) {
                    successCount.getAndIncrement();
                } else {
                    failCount.getAndIncrement();
                }
            }
        });

        thread.start();
        thread.join();
        skierApi.close();

        long end = System.currentTimeMillis();
        System.out.println("Number of successful requests: " + successCount.get());
        System.out.println("Number of unsuccessful requests: " + failCount.get());
        System.out.println("Total run time (milliseconds): " + (end - start));
    }


}
