package fr.itldev.koya.model.impl;

/**
 *
 *
 */
public class GeoPos {

    private Double latitude;
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public GeoPos() {
    }

    public GeoPos(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
