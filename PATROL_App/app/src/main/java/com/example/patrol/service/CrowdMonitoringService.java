package com.example.patrol.service;

import android.location.Address;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.patrol.ApiResponse;
import com.example.patrol.BroadcastItem;
import com.example.patrol.DTO.CrowdLocationDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrowdMonitoringService {
    private static final String TAG = "CrowdMonitoringService";
    private static CrowdMonitoringService instance;

    private static final double MILES_TO_METERS = 1609.34; // Conversion factor from miles to meters
    private static final double RADIUS_MILES = 2.0; // Radius of the circle in miles

    private List<CrowdLocationDTO> crowdLocationDTODeviceList = new ArrayList<>();

    public static CrowdMonitoringService getInstance() {
        if (instance == null) {
            instance = new CrowdMonitoringService();
        }
        return instance;
    }
//    public List<CrowdLocationDTO> getCrowdLocationData(Address searchAddress) {
//        //Create Dummy Data
//        Log.d(TAG,">> getCrowdLocationData");
//        Log.d(TAG,">> getCrowdLocationData Lat Long "+searchAddress.getLatitude()+" "+searchAddress.getLongitude());
//        Log.d(TAG,">> getCrowdLocationData Country name"+searchAddress.getCountryName());
//        Log.d(TAG,">> getCrowdLocationData getPostalCode "+searchAddress.getPostalCode());
//        Log.d(TAG,">> getCrowdLocationData getMaxAddressLineIndex "+searchAddress.getMaxAddressLineIndex());
//        Log.d(TAG,">> getCrowdLocationData getAddressLine"+searchAddress.getAddressLine(0));
//        List<CrowdLocationDTO> crowdLocationDTOList = new ArrayList<>();
//        HttpService httpService = HttpService.getInstance();
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null) {
//            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
//                String idToken = getTokenResult.getToken();
//                Log.d(TAG, "getCrowdLocationData token: " + idToken);
//                ExecutorService executor = Executors.newSingleThreadExecutor();
//
//                executor.execute(() -> {
//                    ApiResponse response = httpService.getCrowdMapData(searchAddress.getLatitude(), searchAddress.getLongitude(), idToken);
//                    new Handler(Looper.getMainLooper()).post(() -> {
//                        if (response != null && response.getStatusCode() == 200) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response.getResponseBody());
//                                JSONArray jsonLocationsArray = jsonObject.getJSONArray("locations");
//                                for (int i = 0; i < jsonLocationsArray.length(); i++) {
//                                    JSONObject jsonLocationObj = jsonLocationsArray.getJSONObject(i);
//                                    double latitude = jsonLocationObj.getDouble("latitude");
//                                    double longitude = jsonLocationObj.getDouble("longitude");
//                                    boolean isInfected = jsonLocationObj.getBoolean("isInfected");
//
//                                    CrowdLocationDTO crowdLocationDTO = new CrowdLocationDTO(latitude, longitude, isInfected);
//                                    crowdLocationDTOList.add(crowdLocationDTO);
//                                }
//                            } catch (JSONException e) {
//                                Log.e(TAG, "Error parsing JSON: " + e.getMessage());
//                                e.printStackTrace();
//                            }
//                        } else {
//                            Log.e(TAG, "Invalid or empty response");
//                        }
//                    });
//                });
//                executor.shutdown();
//
//            });
//        }
//        //create Dummy Data
//        Log.d(TAG, "crowdLocationDTO size "+crowdLocationDTOList.size());
//
//        return crowdLocationDTOList;
//
//        //return getDummyCrowdLocationData(searchAddress.getLatitude(),searchAddress.getLongitude());
//    }

//    public List<CrowdLocationDTO> getCrowdLocationDeviceData(Location deviceLocation) {
//        //Create Dummy Data
//        Log.d(TAG,">> getCrowdLocationDeviceData");
//        Log.d(TAG,">> getCrowdLocationDeviceData Lat Long "+deviceLocation.getLatitude()+" "+deviceLocation.getLongitude());
//        List<CrowdLocationDTO> crowdLocationDTOList = new ArrayList<>();
//        HttpService httpService = HttpService.getInstance();
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null) {
//            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
//                String idToken = getTokenResult.getToken();
//                Log.d(TAG, "getCrowdLocationDeviceData token: " + idToken);
//                ExecutorService executor = Executors.newSingleThreadExecutor();
//
//                executor.execute(() -> {
//                    ApiResponse response = httpService.getCrowdMapData(deviceLocation.getLatitude(), deviceLocation.getLongitude(), idToken);
//                    if (response != null && response.getStatusCode() == 200) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response.getResponseBody());
//                                Log.d(TAG, "getCrowdLocationDeviceData: "+jsonObject.toString());
//                                JSONArray jsonLocationsArray = jsonObject.getJSONArray("locations");
//                                Log.d(TAG,"getCrowdLocationDeviceData "+jsonLocationsArray.length());
//                                for (int i = 0; i < jsonLocationsArray.length(); i++) {
//                                    JSONObject jsonLocationObj = jsonLocationsArray.getJSONObject(i);
//                                    double latitude = jsonLocationObj.getDouble("latitude");
//                                    double longitude = jsonLocationObj.getDouble("longitude");
//                                    boolean isInfected = jsonLocationObj.getBoolean("isInfected");
//
//                                    CrowdLocationDTO crowdLocationDTO = new CrowdLocationDTO(latitude, longitude, isInfected);
//                                    //Log.d(TAG, crowdLocationDTO.toString());
//                                    crowdLocationDTOList.add(crowdLocationDTO);
//                                }
//                                Log.d(TAG,"getCrowdLocationDeviceData all parsing done");
//
//                            } catch (JSONException e) {
//                                Log.e(TAG, "Error parsing JSON: " + e.getMessage());
//                                e.printStackTrace();
//                            }
//                    } else {
//                            Log.e(TAG, "Invalid or empty response");
//                    }
//                    Log.d(TAG,"getCrowdLocationDeviceData getting out of 200 IF");
//
//                    Log.d(TAG,"getCrowdLocationDeviceData end of executor");
//
//                });
//                Log.d(TAG,"getCrowdLocationDeviceData shutting executor");
//
//                executor.shutdown();
//                Log.d(TAG,"getCrowdLocationDeviceData shutted executor");
//
//            });
////            Log.d(TAG, "getCrowdLocationDeviceData out of firebase onclick: ");
//
//        }
////        Log.d(TAG, "getCrowdLocationDeviceData out of firebase if ");if
//
//        //create Dummy Data
//        Log.d(TAG,"getCrowdLocationDeviceData "+crowdLocationDTOList.size());
//
//        return crowdLocationDTOList;
//
//        //return getDummyCrowdLocationData(searchAddress.getLatitude(),searchAddress.getLongitude());
//    }

//    public List<CrowdLocationDTO> getCrowdLocationDTODeviceList (){
//        return crowdLocationDTODeviceList;
//    }
//    public List<CrowdLocationDTO> getCrowdLocationDeviceData(Location deviceLocation) {
//        //Create Dummy Data
//        HttpService httpService = HttpService.getInstance();
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if(firebaseUser != null) {
//            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
//                String idToken = getTokenResult.getToken();
//                ExecutorService executor = Executors.newSingleThreadExecutor();
//                executor.execute(() -> {
//                    ApiResponse response = httpService.getCrowdMapData(deviceLocation.getLatitude(), deviceLocation.getLongitude(), idToken);
//                    new Handler(Looper.getMainLooper()).post(() -> {
//                        if (response != null && response.getStatusCode() == 200) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response.getResponseBody());
//                                Log.d(TAG, "getCrowdLocationDeviceData: "+jsonObject.toString());
//                                JSONArray jsonLocationsArray = jsonObject.getJSONArray("locations");
//                                Log.d(TAG,"getCrowdLocationDeviceData "+jsonLocationsArray.length());
//
//                                for (int i = 0; i < jsonLocationsArray.length(); i++) {
//                                    JSONObject jsonLocationObj = jsonLocationsArray.getJSONObject(i);
//                                    double latitude = jsonLocationObj.getDouble("latitude");
//                                    double longitude = jsonLocationObj.getDouble("longitude");
//                                    boolean isInfected = jsonLocationObj.getBoolean("isInfected");
//
//                                    CrowdLocationDTO crowdLocationDTO = new CrowdLocationDTO(latitude, longitude, isInfected);
//                                    //Log.d(TAG, crowdLocationDTO.toString());
//                                    crowdLocationDTODeviceList.add(crowdLocationDTO);
//                                    Log.d(TAG, "crowd1 " + crowdLocationDTODeviceList.size());
//                                }
//                            } catch (JSONException e) {
//                                Log.e(TAG, "Error parsing JSON: " + e.getMessage());
//                                e.printStackTrace();
//                            }
//                        } else {
//                            Log.e(TAG, "Invalid or empty response");
//                        }
//                        Log.d(TAG,"crowd2:" + crowdLocationDTODeviceList.size());
//                    });
//                });
//                executor.shutdown();
//            });
//        }
//        Log.d(TAG,"crowd data: " + crowdLocationDTODeviceList.size() );
//        return crowdLocationDTODeviceList;
//    }

//    public List<CrowdLocationDTO> getCrowdLocationDeviceData(Location location) {
//        //Create Dummy Data
//        Log.d(TAG,">> getCrowdLocationDataFromLastKnownLocation");
//        Log.d(TAG,">> getCrowdLocationData Lat Long "+location.getLatitude()+" "+location.getLongitude());
//
//        //create Dummy Data
//
//
//        return getDummyCrowdLocationData(location.getLatitude(),location.getLongitude());
//    }

    private List<CrowdLocationDTO> getDummyCrowdLocationData(double searchLat,double searchLong) {
        Log.d(TAG,">> getDummyCrowdLocationData");


        // Generate 20 random points within 2-mile radius
        List<CrowdLocationDTO> crowdLocations = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            double randomRadius = Math.sqrt(random.nextDouble()) * RADIUS_MILES / MILES_TO_METERS;
            double randomAngle = random.nextDouble() * 2 * Math.PI;
            double randomLat = searchLat + randomRadius * Math.cos(randomAngle);
            double randomLong = searchLong + randomRadius * Math.sin(randomAngle);
            boolean isInfected = i < 5; // Mark first 5 locations as infected
            crowdLocations.add(new CrowdLocationDTO(randomLat, randomLong, isInfected));
        }
        Log.d(TAG,"<< getDummyCrowdLocationData "+crowdLocations.size());

        return crowdLocations;
    }


}
