package api;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
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

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 */
public class ApiClient implements Closeable {

    private static final Gson gson = new Gson();

    private static final DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);

    private final CloseableHttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClientBuilder.create().setRetryHandler(retryHandler).build();
    }

    public String escapeString(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    public int postRequest(String baseUrl, String path, Object body) {
        // Create post request
        String url = baseUrl + path;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");

        // Set request body
        String requestBody = gson.toJson(body);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        // Create post response
        CloseableHttpResponse response = null;
        try {
            // Execute request and get response
            response = httpClient.execute(httpPost);

            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                System.out.println("[WARNING] Request failed: " + response.getStatusLine());
            }

            // Get response body
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseBody = EntityUtils.toString(entity);
                // System.out.println(responseBody);
            }
            // Return status code
            return statusCode;
        } catch (IOException e) {
            // If server throws IOException, meaning server error, return 500 (internal server error)
            System.out.println("[SEVERE] Fatal transport error: " + e.getMessage());
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } finally {
            // Close HttpResponse
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                System.out.println("[SEVERE] Error closing HttpResponse: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        // Close HttpClient
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            System.out.println("[SEVERE] Error closing HttpClient: " + e.getMessage());
        }
    }

}
