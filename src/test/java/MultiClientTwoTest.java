import org.junit.Test;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class MultiClientTwoTest {

    @Test
    public void testExecutionTime() throws InterruptedException {
//        testWithPostReqEachThreadSecond(100, 1680);
//        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(500, 336);
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(1000, 168);
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(2000, 84);
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(3000, 56);
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(4200, 40);
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(5250, 32);
        Thread.sleep(1000);
        testWithPostReqEachThreadSecond(6000, 28);
//        Thread.sleep(1000);
//        testWithPostReqEachThreadSecond(12000, 14);
    }

    private void testWithPostReqEachThreadSecond(int postReqEachThreadSecond, int threadsNumSecond) throws InterruptedException {
        MultiClientTwo.main(new String[]{String.valueOf(postReqEachThreadSecond), String.valueOf(threadsNumSecond)});
    }

}