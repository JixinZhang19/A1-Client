package model;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierTask {

    private Integer resortID;

    private String seasonID;

    private String dayID;

    private Integer skierID;

    private Integer time;

    private Integer liftID;

    public SkierTask(Integer resortID, String seasonID, String dayID, Integer skierID, Integer time, Integer liftID) {
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
        this.time = time;
        this.liftID = liftID;
    }

    public Integer getResortID() {
        return resortID;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public String getDayID() {
        return dayID;
    }

    public Integer getSkierID() {
        return skierID;
    }

    public Integer getTime() {
        return time;
    }

    public Integer getLiftID() {
        return liftID;
    }

}
