import api.SkiersApi;
import model.LifeRide;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 * <p>
 * 1. 10000个请求 + shared HttpClient: 耗时1700ms -> 0.17 ms/req -> expect throughput: 6 req/ms
 * 2. 10000个请求 + individual HttpClient: 耗时5000+ms (x)
 */
public class SingleClient {

    /*    private final static Gson gson = new Gson();
        private final static DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);*/
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        Integer resortID = 12; // Integer | ID of the resort the skier is at
        String seasonID = "2019"; // String | ID of the ski season
        String dayID = "1"; // String | ID number of ski day in the ski season
        Integer skierID = 19; // Integer | ID of the skier riding the lift
        LifeRide lifeRide = new LifeRide(111, 222);

        SkiersApi skiersApi = new SkiersApi();
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                int code = skiersApi.writeNewLiftRideCall(lifeRide, resortID, seasonID, dayID, skierID);
                if (code == 201) {
                    successCount.getAndIncrement();
                } else {
                    failCount.getAndIncrement();
                }
/*                CloseableHttpClient httpClient = HttpClientBuilder.create()
                        .setRetryHandler(retryHandler)
                        .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                        .build();

                String path = "/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}"
                        .replaceAll("\\{" + "resortID" + "\\}", resortID.toString())
                        .replaceAll("\\{" + "seasonID" + "\\}", seasonID)
                        .replaceAll("\\{" + "dayID" + "\\}", dayID)
                        .replaceAll("\\{" + "skierID" + "\\}", skierID.toString());
                String url = "http://localhost:8080" + path;
                HttpPost httpPost = new HttpPost(url);

                String requestBody = gson.toJson(lifeRide);
                StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
                httpPost.setEntity(requestEntity);

                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    // 获取响应状态码
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 201) {
                        System.err.println("Method failed: " + response.getStatusLine());
                    } else {
                        // System.out.println(response.getStatusLine());
                    }
                    // 获取响应实体
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        // 从响应实体中获取响应内容
                        String responseBody = EntityUtils.toString(entity);
                        // System.out.println(responseBody);
                    }
                } catch (IOException e) {
                    System.err.println("Fatal transport error: " + e.getMessage());
                    e.printStackTrace();
                }*/
            }
        });
        thread.start();
        thread.join(); // 等待线程完成
        long end = System.currentTimeMillis();
        System.out.println("time(ms): " + (end - start) + " success req: " + successCount.get() + " fail req: " + failCount.get());
    }


}
