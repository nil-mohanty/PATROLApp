package com.example.patrol.enums;

public enum ClaimType {
    GENERAL_USER("GENERAL USER", "GEN"),
    GOVERNMENT_OFFICER("GOVERNEMENT OFFICER", "GOVT"),
    RESEARCHER("RESEARCHER", "RES"),
    ECOMMERCE_AGENT("ECOMMERCE AGENT", "ECOMM");

    private final String description;
    private final String code;

    ClaimType(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ClaimType fromDescription(String description) {
        for (ClaimType claim : ClaimType.values()) {
            if (claim.description.equalsIgnoreCase(description)) {
                return claim;
            }
        }
        throw new IllegalArgumentException("No constant with description " + description + " found");
    }
}
