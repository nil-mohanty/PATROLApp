package com.example.patrol.DTO;

public class User {
    private String email;
    private String first_name;
    private String last_name;
    private String uuid;
    private String uuid_hash;
    private String password;
    private String role_name;

    private String fcm_reg_token;

    // Constructor
    public User(String email, String firstName, String lastName, String password, String role, String fcmRegToken) {
        this.email = email;
        this.first_name = firstName;
        this.last_name = lastName;
        this.password = password;
        this.role_name = role;
        this.fcm_reg_token = fcmRegToken;
    }

    public String getFullName() {
        return first_name + " " + last_name;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFcm_reg_token() {
        return fcm_reg_token;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid_hash() {
        return uuid_hash;
    }

    public void setUuid_hash(String uuid_hash) {
        this.uuid_hash = uuid_hash;
    }
}
