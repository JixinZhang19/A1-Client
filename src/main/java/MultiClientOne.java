import api.SkierApi;
import model.LifeRide;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 * <p>
 * 168 * 1000 -> 13262 ms -> 15 req/ms
 * 84 * 2000 -> slower
 */
// todo
//  done 1) producer-consumer模式：单个producer线程（永远不必等待事件可用，消耗尽可能少的CPU和内存）随机生成post请求参数和内容，多个consumer线程发起请求
//  done 2) 32个线程 * 每个线程1000个请求 -> 创建多少个线程 * 每个线程多少个请求 performance最好（只需要保证一共发送200K个请求）-> test case
//  done 2.1）新的问题：httpClient 相关，关于连接的关闭策略，如何确保每个线程发送X个post请求后关闭连接；如果同一个线程被复用，连接可以不关闭？
//            -> response的关闭 和 httpClient的关闭 和 threadLocal的关闭
//  done 3) IOException处理，并在最外层统计成功的请求数和不成功的请求数，原子类count来解决并发问题
//  4) expect throughput (little's rule) & real throughput
//  5) newFixedThreadPool 和 newCachedThreadPool 哪个更好？newFixedThreadPool 的个数设为多少更好？有没有更好的threadPool，貌似不建议用这俩

// todo: 服务端线程使用情况和客户端线程数/httpClient数的关系

public class MultiClientOne {

    private static final ExecutorService executor = Executors.newFixedThreadPool(250);
    private static final SkierApi skiersApi = new SkierApi();
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final CountDownLatch startLatch = new CountDownLatch(1);

    // todo: InterruptedException 如何处理，总结所有出现的异常的类型，以及是否都处理了？
    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        Integer resortID = 12;
        String seasonID = "2019";
        String dayID = "1";
        Integer skierID = 19;
        LifeRide lifeRide = new LifeRide(111, 222);

        for (int i = 0; i < 32; i++) {
            executor.execute(() -> {
                // 在每个线程中调用 1000 次 api.SkiersApi 的 POST 方法
                for (int k = 0; k < 1000; k++) {
                    int code = skiersApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                    if (code == 201) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                }
                // 一旦任何一个线程完成，释放 startLatch
                startLatch.countDown();
            });
        }

        startLatch.await();

        for (int i = 0; i < 168; i++) {
            executor.execute(() -> {
                // 在每个线程中调用 1000 次 api.SkiersApi 的 POST 方法
                for (int k = 0; k < 1000; k++) {
                    int code = skiersApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                    if (code == 201) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        // 等待线程池中所有任务执行完毕或者等待1分钟
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("线程池等待超时，可能有任务未完成");
        }

        long end = System.currentTimeMillis();
        System.out.println("time(ms): " + (end - start) + " success req: " + successCount.get() + " fail req: " + failCount.get());
    }

}
