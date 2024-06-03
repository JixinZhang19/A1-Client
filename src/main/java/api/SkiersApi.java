package api;

import model.LifeRide;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-01
 */
public class SkiersApi {

    private static final ThreadLocal<ApiClient> apiClients = ThreadLocal.withInitial(ApiClient::new);

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

}
