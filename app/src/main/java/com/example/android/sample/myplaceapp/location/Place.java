package com.example.android.sample.myplaceapp.location;

/**
 * 位置情報のエンティティ。
 */
public class Place {

    public static final String DATE_STR_FORMAT = "%1$tF";

    /**
     * 主キー。
     */
    private long id;

    /**
     * 緯度。
     */
    private double latitude;

    /**
     * 経度。
     */
    private double longitude;

    /**
     * この位置情報が取得された時間。
     */
    private long time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
