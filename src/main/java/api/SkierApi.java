package api;

import model.LifeRide;

import java.io.Closeable;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 */
public class SkierApi implements Closeable {

    private static final SkierThreadLocal<ApiClient> apiClients = new SkierThreadLocal<>();

    // Change the baseUrl once IP changes
    private static final String baseUrl = "http://34.211.96.209:8080/A1-Server_war";

    public int writeNewLiftRideCall(LifeRide lifeRide, Integer resortID, String seasonID, String dayID, Integer skierID) {

        // Get ApiClient instance of this thread
        ApiClient apiClient = apiClients.get();

        // Construct path
        String path = "/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}"
                .replaceAll("\\{" + "resortID" + "}", apiClient.escapeString(resortID.toString()))
                .replaceAll("\\{" + "seasonID" + "}", apiClient.escapeString(seasonID))
                .replaceAll("\\{" + "dayID" + "}", apiClient.escapeString(dayID))
                .replaceAll("\\{" + "skierID" + "}", apiClient.escapeString(skierID.toString()));

        return apiClient.postRequest(baseUrl, path, lifeRide);
    }

    @Override
    public void close() {
        apiClients.remove();
    }

}
