package com.example.patrol.homePage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.patrol.ApiResponse;
import com.example.patrol.BLE.BLEJobService;
import com.example.patrol.BroadcastGenUserActivity;
import com.example.patrol.DTO.User;
import com.example.patrol.MonitorCrowdActivity;
import com.example.patrol.ProfileActivity;
import com.example.patrol.R;
import com.example.patrol.RequestItemsActivity;
import com.example.patrol.UpdateHealthStatusActivity;
import com.example.patrol.service.HttpService;
import com.example.patrol.service.LocationHeartbeatService;
import com.example.patrol.service.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenUserHomeActivity extends AppCompatActivity {

    private String TAG = "Gen User Home Activity";
    private FirebaseUser firebaseUser;
    private HttpService httpService;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gen_user_home_page);

        // UI components initialization
        CardView progressBarView = findViewById(R.id.gen_progress_circular_view);
        progressBarView.setVisibility(View.VISIBLE);
        Button buttonProfile = findViewById(R.id.buttonGenUserProfile);
        Button buttonHealthStatus = findViewById(R.id.buttonUpdateGenHealthStatus);
        Button buttonMonitor = findViewById(R.id.buttonGenUserMonitorCrowd);
        Button buttonBroadcasts = findViewById(R.id.buttonGenUserBroadcast);
        Button buttonRequestItems = findViewById(R.id.buttonGenUserRequestItems);


        // Service initialization
        this.httpService = HttpService.getInstance();
        this.gson = new Gson();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {

            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                // We need to start all the services inside this callback
                // else the time when idToken will be populated is non-deterministic
                String idToken = getTokenResult.getToken();
                ExecutorService executor = Executors.newSingleThreadExecutor();

                // Get Firebase Notification Registration Token
                String token = MyFirebaseMessagingService.getToken(this);
                if(!token.equals("empty")) {
                    executor.execute(() -> {
                        ApiResponse response = httpService.updateFCMRegistrationToken(
                                firebaseUser.getEmail(),
                                token,
                                idToken
                        );
                        Log.d(TAG, "onCreate: " + response.getResponseBody());
                    });
                }

                executor.execute(() -> {
                    ApiResponse response = httpService.getUserProfileData(firebaseUser.getEmail(), idToken);
                    User user = gson.fromJson(response.getResponseBody(), User.class);
                    // Start BLE Scanning and Advertising service
                    if(!isServiceRunningInForeground(getApplicationContext(), BLEJobService.class)) {
                        Intent intent = new Intent(this, BLEJobService.class);
                        intent.putExtra("uuid", user.getUuid());
                        startService(intent);
                    }

                    buttonProfile.setOnClickListener(v -> {
                        Log.d(TAG, "Load profile");
                        Intent intent = new Intent(GenUserHomeActivity.this, ProfileActivity.class);
                        intent.putExtra("fullName", user.getFullName());
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("role", user.getRole_name());
                        startActivity(intent);
                    });

                });
                executor.shutdown();

                // Pass data from user object  accordingly as required
                buttonHealthStatus.setOnClickListener(v -> {
                    Log.d(TAG, "Load health status");
                    Intent intent = new Intent(GenUserHomeActivity.this, UpdateHealthStatusActivity.class);
                    startActivity(intent);
                });

                buttonMonitor.setOnClickListener(v -> {
                    Log.d(TAG, "Load crowd monitor");
                    Intent intent = new Intent(GenUserHomeActivity.this, MonitorCrowdActivity.class);
                    startActivity(intent);
                });

                buttonBroadcasts.setOnClickListener(v -> {
                    Log.d(TAG, "Load broadcast page");
                    Intent intent = new Intent(GenUserHomeActivity.this, BroadcastGenUserActivity.class);
                    startActivity(intent);
                });

                buttonRequestItems.setOnClickListener(v -> {
                    Log.d(TAG, "Load survey page");
                    Intent intent = new Intent(GenUserHomeActivity.this, RequestItemsActivity.class);
                    startActivity(intent);
                });
                progressBarView.setVisibility(View.GONE);
            });
        }

        // Start location service
        if(!isServiceRunningInForeground(this, LocationHeartbeatService.class)) {
            Intent intent = new Intent(this, LocationHeartbeatService.class);
            startService(intent);
        }

    }

    private boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }


}