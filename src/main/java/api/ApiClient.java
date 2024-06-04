package api;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 */
public class ApiClient implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());
    private final static Gson gson = new Gson();
    private final static DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);
    private final CloseableHttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClientBuilder.create().setRetryHandler(retryHandler).build();
    }

    public String escapeString(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    public int postRequest(String baseUrl, String path, Object body) {
        // 创建 HTTP POST 请求
        String url = baseUrl + path;
        HttpPost httpPost = new HttpPost(url);
        // 设置请求体
        String requestBody = gson.toJson(body);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            // 获取响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 201) {
                LOGGER.log(Level.WARNING, "Method failed: " + response.getStatusLine());
            }
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 从响应实体中获取响应内容
                String responseBody = EntityUtils.toString(entity);
            }
            return statusCode;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fatal transport error: " + e.getMessage(), e);
            return 500;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing HttpResponse: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
        // System.out.println("close HttpClient");
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing HttpClient: " + e.getMessage(), e);
        }
    }

}
