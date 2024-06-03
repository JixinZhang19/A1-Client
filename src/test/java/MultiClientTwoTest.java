import org.junit.Test;

/*
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class MultiClientTwoTest {

    @Test
    public void testExecutionTime() throws InterruptedException {
//        testWithPostReqEachThreadSecond(100, 1680);
//        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(500, 336); // 8624 12797
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(1000, 168); // 7349 10961
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(2000, 84); // 5400 6688
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(3000, 56); // 5605 5294
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(4200, 40); // 5776 5669
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(5250, 32); // 25812 22318
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(6000, 28); // 7560 9335
//        Thread.sleep(1000);
//        testWithPostReqEachThreadSecond(12000, 14);
    }

    private void testWithPostReqEachThreadSecond(int postReqEachThreadSecond, int threadsNumSecond) throws InterruptedException {
        MultiClientTwo.main(new String[]{String.valueOf(postReqEachThreadSecond), String.valueOf(threadsNumSecond)});
    }

}