package api;

import model.LifeRide;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 */
public class SkierApi implements Closeable {

    // private static final ThreadLocal<ApiClient> apiClients = ThreadLocal.withInitial(ApiClient::new);
    private static final SkierThreadLocal<ApiClient> apiClients = new SkierThreadLocal<>();

    public int writeNewLiftRideCall(LifeRide lifeRide, Integer resortID, String seasonID, String dayID, Integer skierID) {

        ApiClient apiClient = apiClients.get();

        String baseUrl = "http://35.92.46.108:8080/A1-Server_war";
        // String baseUrl = "http://localhost:8080";

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
