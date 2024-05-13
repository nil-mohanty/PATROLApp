package com.example.patrol.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.patrol.ApiResponse;
import com.example.patrol.DTO.LocationHistory;

import com.example.patrol.utils.UtilityService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationHeartbeatService extends Service {
    static final String TAG = "HEARTBEAT";
    private HttpService httpService;
    private Gson gson;
    private String userEmail;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.httpService = HttpService.getInstance();
        this.gson = new Gson();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            this.userEmail = user.getEmail();
            user.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                startLocationUpdates(idToken);
            });
            Toast.makeText(
                    getApplicationContext(), "Location Service has started running in the background",
                    Toast.LENGTH_SHORT
            ).show();
        }
        return START_STICKY;
    }

    private void startLocationUpdates(String idToken) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        long minuteValue = 1;
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(minuteValue * 60 * 1000) // Every 30 mins
                .setFastestInterval(minuteValue * 60 * 1000) // Fastest interval in milliseconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                long currentTimeMillis = System.currentTimeMillis();

                // Create a SimpleDateFormat object with ISO 8601 format
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String isoTimestamp = new UtilityService().getCurrentTimeStamp();
                Log.d(TAG, "onLocationResult: isoTimestamp "+isoTimestamp);
                for (Location location : locationResult.getLocations()) {
                    // Send location to server
                    LocationHistory locationHistory = new LocationHistory(
                            userEmail,
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()),
                            isoTimestamp
                    );

                    String locationHistoryJson = gson.toJson(locationHistory);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        ApiResponse response = httpService.sendLocation(locationHistoryJson, idToken);
                        Log.d(TAG, "onLocationResult: " + response.getResponseBody());
                    });
                    executor.shutdown();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }
    @Override
    public boolean stopService(Intent name) {
        Log.d("Stopping", "Stopping Service");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(
                getApplicationContext(), "Service execution completed",
                Toast.LENGTH_SHORT
        ).show();
        Log.d("Stopped", "Service Stopped");
        super.onDestroy();
    }

}
