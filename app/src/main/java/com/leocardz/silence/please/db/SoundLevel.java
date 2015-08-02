package com.leocardz.silence.please.db;

/**
 * Created by santhosh on 3/8/15.
 */
public class SoundLevel {

    private long quietTime;
    private long groupTime;
    private long noiseTime;
    private long date;
    private long rowId;

    public SoundLevel(long rowID, long quietTime, long groupTime, long noiseTime, long date) {
        this.rowId = rowID;
        this.quietTime = quietTime;
        this.groupTime = groupTime;
        this.noiseTime = noiseTime;
        this.date = date;
    }
    public void setQuietTime(long quietTime) {
        this.quietTime = quietTime;
    }

    public void setGroupTime(long groupTime) {
        this.groupTime = groupTime;
    }

    public void setNoiseTime(long noiseTime) {
        this.noiseTime = noiseTime;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getRowId() {
        return rowId;
    }

    public long getQuietTime() {
        return quietTime;
    }

    public long getGroupTime() {
        return groupTime;
    }

    public long getNoiseTime() {
        return noiseTime;
    }

    public long getDate() {
        return date;
    }

}
