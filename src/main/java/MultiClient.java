import model.SkierTask;
import producerConsumer.SkierConsumer;
import producerConsumer.SkierProducer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 *
 * 86308 ms
 */
// todo:
//  done 1) producer-consumer模式：单个producer线程（永远不必等待事件可用，消耗尽可能少的CPU和内存）随机生成post请求参数和内容，多个consumer线程消费并发起请求
//  需要远端测试 2) 32个线程 * 每个线程1000个请求 -> 创建多少个线程 * 每个线程多少个请求 performance最好（只需要保证一共发送200K个请求）-> test case 不现实，远程太慢了
//  done 2.1）httpClient 相关，关于连接的关闭策略，如何确保每个线程发送完1000个post请求后（完成任务后）关闭连接；如果同一个线程被复用，连接可以不关闭吗？
//            -> response的关闭（每个请求结束后）；httpClient的关闭 <- threadLocal的remove（每个线程任务完成后）
//  done 3) IOException处理，并在最外层统计成功的请求数和不成功的请求数，原子类count来解决并发问题
//  需要远端测试 4) expect throughput (little's rule) & real throughput
//  需要远端测试 5) newFixedThreadPool 和 newCachedThreadPool 哪个更好？newFixedThreadPool 的线程个数设为多少更好？
//  6) 服务端线程启用情况和客户端线程数/httpClient数的关系？server端thread pool如何设置？
//  7) InterruptedException 如何处理，所有出现的异常是否都处理了?
//  8) 将sout都改为log, 写一个全局logger factory共用还是每个类一个logger?
public class MultiClient {


    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final BlockingQueue<SkierTask> queue = new LinkedBlockingQueue<>();
    private static final CountDownLatch startLatch = new CountDownLatch(1);
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    private static final int POST_REQ_EACH_THREAD_FIRST = 1000;
    private static final int THREADS_NUM_FIRST = 32;
    private static final int POST_REQ_EACH_THREAD_SECOND = 1000;
    private static final int THREADS_NUM_SECOND = 168;


    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        // start 1 skier producer
        (new Thread(new SkierProducer(queue))).start();

        // start 32 skier consumers
        for (int i = 0; i < THREADS_NUM_FIRST; i++) {
            executor.execute(new SkierConsumer(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_FIRST));
        }

        startLatch.await();

        // start remaining skier consumers
        for (int i = 0; i < THREADS_NUM_SECOND; i++) {
            executor.execute(new SkierConsumer(queue, startLatch, successCount, failCount, POST_REQ_EACH_THREAD_SECOND));
        }

        executor.shutdown();
        // 等待线程池中所有任务执行完毕或者等待1分钟
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            // 如果超时，则输出提示信息
            System.out.println("线程池等待超时，可能有任务未完成");
        }

        long end = System.currentTimeMillis();
        System.out.println("time(ms): " + (end - start) + " success req: " + successCount.get() + " fail req: " + failCount.get());

        // todo: 再启动另一个线程池，多个fileConsumer写入csv文件



    }

}
