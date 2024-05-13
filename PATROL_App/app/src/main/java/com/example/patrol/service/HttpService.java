package com.example.patrol.service;

import android.os.Environment;
import android.util.Log;

import com.example.patrol.enums.DownloadEnums;
import com.example.patrol.utils.UtilityService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.patrol.ApiResponse;
import com.google.android.gms.common.api.Api;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpService {

    private OkHttpClient client;
    private String TAG = "HTTP SERVICE";
    private static HttpService INSTANCE = null;
    private static UtilityService utilityService;
//    private static final String host = "http://192.168.159.194:5001";
    private static final String host = "https://patrol-fawn.vercel.app";

    public static final String DOWNLOAD_LOCATION_HISTORY_URL = "/research/location_history";
    public static final String DOWNLOAD_INFECTION_HISTORY_URL = "/research/infection_history";
    public static final String DOWNLOAD_VACCINATION_DATA_URL = "/research/vaccination_history";
    public static final String DOWNLOAD_BROADCASTED_MESSAGES_URL = "/research/broadcast_history";
    public static final String DOWNLOAD_ECOMMERCE_INSIGHTS_URL = "/research/ecommerce_history";
    public static final String MONITOR_MAP_URL = "/crowd/map/monitor";
    public static final String SEND_ITEMS_RANKING_URL = "/user/request_items";


    private HttpService() {
        this.client = new OkHttpClient();
        utilityService = new UtilityService();
    }

    public static synchronized HttpService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new HttpService();
        }
        return INSTANCE;
    }

    public ApiResponse updateFCMRegistrationToken(String userEmail, String fcmRegToken, String idToken) {
        String url = host + "/user/update_fcm_reg_token";
        JSONObject payload = new JSONObject();
        try {
            payload.put("user_email", userEmail);
            payload.put("fcm_reg_token", fcmRegToken);
        } catch (JSONException e) {
            Log.e(TAG, "getUserProfileData: ", e);
        }
        RequestBody requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + idToken)
                .put(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to send location " + e);
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }
    public ApiResponse sendLocation(String bodyJson, String idToken) {
        String url = host + "/user/populate_location";
        RequestBody requestBody = RequestBody.create(bodyJson, MediaType.parse("application/json"));
        String bearerToken = "Bearer " + idToken;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", bearerToken)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to send location " + e);
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse createUser(String personJson) {
        String url = host +"/user/create";
        RequestBody requestBody = RequestBody.create(personJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Registration failed. " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse getUserProfileData(String userEmail, String idToken) {
        String url = host + "/user/info";
        String bearerToken = "Bearer " + idToken;
        JSONObject payload = new JSONObject();
        try {
            payload.put("user_email", userEmail);
        } catch (JSONException e) {
            Log.e(TAG, "getUserProfileData: ", e);
        }
        RequestBody requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", bearerToken)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Data reception failed: " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public String sendRequestItems(Map<String,Integer> itemsMap, String selectedArea, String idToken) {
        String url = host + SEND_ITEMS_RANKING_URL;
        JSONObject payload = new JSONObject();
        try {
            JSONArray itemsArray = new JSONArray();
            for (String itemName : itemsMap.keySet()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("itemName", itemName);
                itemObject.put("quantity",itemsMap.get(itemName) );
                itemsArray.put(itemObject);
            }
            payload.put("items", itemsArray);
            payload.put("city",selectedArea);
            //TODO - Cleanup the user_id from email to sth else
            payload.put("user_email", utilityService.getCurrentUser());
            payload.put("timestamp", utilityService.getCurrentTimeStamp());
        } catch (JSONException e) {
            Log.e(TAG, "sendRequestItems: " + e.toString());
            return e.toString();
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), payload.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization","Bearer "+idToken)
                .build();
        Log.d(TAG, "sendRequestItems: url " + request.url());
        Log.d(TAG, "sendRequestItems: token " + request.header("Authorization"));
        Log.d(TAG, "sendRequestItems: payload " + payload);

        String message = "Could not save response.";
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String jsonString = responseBody.string();
                    JSONObject jsonObject = new JSONObject(jsonString);

                    message = jsonObject.getString("message");
                    JSONObject requestPayload = jsonObject.getJSONObject("requestPayload");

                    Log.d(TAG, "sendRequestItems Payload " + requestPayload);
                    Log.d(TAG, "sendRequestItems msg " + message);
                }
            } else {
                Log.e(TAG, "sendRequestItems Error: " + response.code() + " " + response.message());
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "sendRequestItems Error: " + e);
        }
        return message;
    }



    public ApiResponse updateUserInfection(String userEmail, boolean infected, List<String> bleHistory, String symptoms, String idToken) {
       JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_email", userEmail);
            jsonBody.put("infected", infected);
            jsonBody.put("symptoms", symptoms);
            jsonBody.put("ble_history", new JSONArray(bleHistory));
            jsonBody.put("timestamp", utilityService.getCurrentTimeStamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(host + "/user/update_infection")
                .post(requestBody)
                .header("Authorization","Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse updateUserVaccination(String userEmail, int day, int month, int year, String idToken) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_email", userEmail);
            jsonBody.put("vaccination_date", year + "-" + (month + 1) + "-" + day);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(host + "/user/update_vaccination")
                .post(requestBody)
                .header("Authorization","Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public String downloadHistory(DownloadEnums entityName, String idToken) {
        String url = host;
        String fileName = "";
        switch (entityName) {
            case LOCATION_HISTORY:
                url = url + DOWNLOAD_LOCATION_HISTORY_URL;
                fileName = "Location_History";
                break;
            case INFECTION_HISTORY:
                url = url + DOWNLOAD_INFECTION_HISTORY_URL;
                fileName = "Infection_History";
                break;
            case VACCINATION_DATA:
                url = url + DOWNLOAD_VACCINATION_DATA_URL;
                fileName = "Vaccination_Data";
                break;
            case BROADCASTED_MESSAGES:
                url = url + DOWNLOAD_BROADCASTED_MESSAGES_URL;
                fileName = "Broadcasted_Messages";
                break;
            case ECOMMERCE_INSIGHTS:
                url = url + DOWNLOAD_ECOMMERCE_INSIGHTS_URL;
                fileName = "Ecommerce_insights";
                break;
        }

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization","Bearer "+idToken)
                .build();

        Log.d(TAG, "downloadHistory: url" + url);
        Log.d(TAG, "downloadHistory: idToken" + idToken);

        String message = "Could not download the file.";
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String jsonString = responseBody.string();
                    try {
                        // Get the directory for the user's public downloads directory.
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName + ".json");

                        FileOutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(jsonString.getBytes());
                        outputStream.close();
                        Log.d(TAG, "File saved: " + file.getAbsolutePath());
                        message = "File Downloaded Successfully.";
                    } catch (IOException e) {
                        Log.e(TAG, "Error saving JSON data to file: " + e.getMessage());
                    }
                    // Access the JSON data

                    Log.d(TAG, "downloadHistory msg " + message);
                }
            } else {
                Log.e(TAG, "downloadHistory Error: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "downloadHistory Error: " + e);
        }
        return message;
    }

    public ApiResponse getBroadcastMessages(String idToken) {
        String url = host + "/message/broadcasts";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization","Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to get broadcast messages: " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse getTrends(String latitude, String longitude, String days, String idToken) {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
            jsonBody.put("days", days);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(host + "/crowd/trend/monitor")
                .post(requestBody)
                .header("Authorization","Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failure while getting trends:  " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse sendBroadcast(String userEmail, String title, String message, String idToken) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_email", userEmail);
            jsonBody.put("title", title);
            jsonBody.put("body", message);
            jsonBody.put("timestamp", utilityService.getCurrentTimeStamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(host + "/message/send_broadcast")
                .post(requestBody)
                .header("Authorization", "Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String responseBody = response.body().string();
            return new ApiResponse(statusCode, responseBody);
        }
        catch (IOException e) {
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse getHeathRecords(String idToken) {
        String url = host + "/government/health_records";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization","Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to get broadcast messages: " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse getSupplyDemand(String city, String idToken) {
        String url = host + "/ecommerce/demand/" + city;


        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization","Bearer "+idToken)
                .build();
        Log.d(TAG, "getSupplyDemand: URL "+url);
        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to get broadcast messages: " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    public ApiResponse getCrowdMapData(double latitude, double longitude, String idToken) {
        String url = host + MONITOR_MAP_URL;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"getCrowdMapData "+jsonBody.toString());
        Log.d(TAG,"getCrowdMapData idToken"+idToken);

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", "Bearer "+idToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String body = response.body().string();
            return new ApiResponse(statusCode, body);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to get getCrowdMapData messages: " + e.toString());
            return new ApiResponse(500, "Internal server error: " + e.getMessage());
        }
    }
}
