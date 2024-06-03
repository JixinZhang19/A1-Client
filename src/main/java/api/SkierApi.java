package api;

import model.LifeRide;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 */
public class SkierApi implements Closeable {

    // 在每个线程执行结束后，ThreadLocal 中的 ApiClient 对象会被垃圾回收器进行回收处理
    // 在使用线程池管理的多个线程中，每个线程的 ThreadLocal 变量副本在执行完任务后不会立即销毁，因为线程池会重用线程
    // private static final ThreadLocal<ApiClient> apiClients = ThreadLocal.withInitial(ApiClient::new);
    private static final SkierThreadLocal<ApiClient> apiClients = new SkierThreadLocal<>();

    public int writeNewLiftRideCall(LifeRide lifeRide, Integer resortID, String seasonID, String dayID, Integer skierID) {

        ApiClient apiClient = apiClients.get();

        String baseUrl = "http://localhost:8080";

        String path = "/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}"
                .replaceAll("\\{" + "resortID" + "}", apiClient.escapeString(resortID.toString()))
                .replaceAll("\\{" + "seasonID" + "}", apiClient.escapeString(seasonID))
                .replaceAll("\\{" + "dayID" + "}", apiClient.escapeString(dayID))
                .replaceAll("\\{" + "skierID" + "}", apiClient.escapeString(skierID.toString()));

        return apiClient.postRequest(baseUrl, path, lifeRide);
    }

    @Override
    public void close() throws IOException {
        apiClients.remove();
    }

}
