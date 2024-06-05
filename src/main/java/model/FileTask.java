package model;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-04
 */
public class FileTask {

    private long startTime;

    private String requestType;

    private long latency;

    private int responseCode;

    public FileTask(long startTime, String requestType, long latency, int responseCode) {
        this.startTime = startTime;
        this.requestType = requestType;
        this.latency = latency;
        this.responseCode = responseCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getRequestType() {
        return requestType;
    }

    public long getLatency() {
        return latency;
    }

    public int getResponseCode() {
        return responseCode;
    }

}
