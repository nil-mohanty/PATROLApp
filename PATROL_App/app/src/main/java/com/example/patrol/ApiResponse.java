package com.example.patrol;

public class ApiResponse {
    private final int statusCode;
    private final String responseBody;

    public ApiResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
