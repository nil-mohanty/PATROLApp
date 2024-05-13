package com.example.patrol.DTO;


public class CrowdLocationDTO {
    private double latitude;
    private double longitude;
    private boolean isInfected;

    // Constructor
    public CrowdLocationDTO(double latitude, double longitude, boolean isInfected) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isInfected = isInfected;
    }

    // Getters and setters
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

    public boolean isInfected() {
        return isInfected;
    }

    public void setInfected(boolean infected) {
        isInfected = infected;
    }

}
