package com.example.patrol.DTO;

public class LocationHistory {
    private final String email;
    private final String latitude;
    private final String longitude;
    private final String timestamp;
    public LocationHistory(String email, String latitude, String longitude, String timestamp) {
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
