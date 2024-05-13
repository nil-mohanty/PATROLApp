package com.example.patrol.enums;

import java.util.HashMap;
import java.util.Map;

public enum CityCoordinates {
    BAY_AREA(37.7749, -122.4194),
    LOS_ANGELES_AREA(34.0522, -118.2437),
    NEW_YORK_AREA(40.7128, -74.0060),
    TEXAS_AREA(31.9686, -99.9018),
    SEATTLE_AREA(47.6062, -122.3321);

    private final double latitude;
    private final double longitude;

    CityCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private static final Map<String, CityCoordinates> stringToEnum = new HashMap<>();
    static {
        for (CityCoordinates city : values()) {
            stringToEnum.put(city.name().replace("_", " ").toUpperCase(), city);
        }
    }

    public static CityCoordinates fromString(String name) {
        return stringToEnum.get(name.toUpperCase());
    }
}
