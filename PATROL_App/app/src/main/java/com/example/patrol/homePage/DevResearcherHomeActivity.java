package com.example.patrol.homePage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.ApiResponse;
import com.example.patrol.DTO.User;
import com.example.patrol.ApiResponse;
import com.example.patrol.MonitorCrowdActivity;
import com.example.patrol.ProfileActivity;
import com.example.patrol.R;
import com.example.patrol.enums.DownloadEnums;
import com.example.patrol.service.HttpService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DevResearcherHomeActivity extends AppCompatActivity {
    private String TAG = "DevResearcherHomeActivity";
    private HttpService httpService;
    private Gson gson;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dev_researcher_landing_page);

        Button buttonProfile = findViewById(R.id.buttonDevResearchProfile);
        Button buttonMonitorCrowd = findViewById(R.id.buttonDevResearchMonitorCrowd);
        Button buttonDownloadLocationHistory = findViewById(R.id.downloadLocationHistory);
        Button buttonDownloadInfectionHistory = findViewById(R.id.downloadInfectionHistory);
        Button buttonDownloadVaccinationData = findViewById(R.id.downloadVaccinationData);
        Button buttonDownloadBroadcastedMessages = findViewById(R.id.downloadBroadcastedMessages);
        Button buttonDownloadEcommerceInsights = findViewById(R.id.downloadEcommerceInsights);

        this.httpService = HttpService.getInstance();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.gson = new Gson();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response = httpService.getUserProfileData(firebaseUser.getEmail(), idToken);
                    User user = gson.fromJson(response.getResponseBody(), User.class);
                    buttonProfile.setOnClickListener(v -> {
                        Log.d(TAG, "Load profile");
                        Intent intent = new Intent(DevResearcherHomeActivity.this, ProfileActivity.class);
                        intent.putExtra("fullName", user.getFullName());
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("role", user.getRole_name());
                        startActivity(intent);
                    });

                });
                executor.shutdown();
            });
        }


        buttonMonitorCrowd.setOnClickListener(v -> {
            Log.d(TAG, "Load crowd monitor");
            Intent intent = new Intent(DevResearcherHomeActivity.this, MonitorCrowdActivity.class);
            startActivity(intent);
        });

        buttonDownloadLocationHistory.setOnClickListener(v -> downloadLocationHistory(DownloadEnums.LOCATION_HISTORY));

        buttonDownloadInfectionHistory.setOnClickListener(v -> downloadLocationHistory(DownloadEnums.INFECTION_HISTORY));

        buttonDownloadVaccinationData.setOnClickListener(v -> downloadLocationHistory(DownloadEnums.VACCINATION_DATA));

        buttonDownloadBroadcastedMessages.setOnClickListener(v -> downloadLocationHistory(DownloadEnums.BROADCASTED_MESSAGES));

        buttonDownloadEcommerceInsights.setOnClickListener(v -> downloadLocationHistory(DownloadEnums.ECOMMERCE_INSIGHTS));


    }

    private void downloadLocationHistory(DownloadEnums entityName) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "downloadLocationHistory token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String response = this.httpService.downloadHistory(entityName,idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(DevResearcherHomeActivity.this, response, Toast.LENGTH_SHORT).show();
                    });
                });
                executor.shutdown();

            });
        }

    }

}
