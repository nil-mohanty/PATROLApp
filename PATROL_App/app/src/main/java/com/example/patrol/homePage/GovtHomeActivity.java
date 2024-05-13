package com.example.patrol.homePage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.patrol.ApiResponse;
import com.example.patrol.BLE.BLEJobService;
import com.example.patrol.BroadcastGovtActivity;
import com.example.patrol.DTO.User;
import com.example.patrol.MonitorCrowdActivity;
import com.example.patrol.MonitorHealthStatusActivity;
import com.example.patrol.ProfileActivity;
import com.example.patrol.R;
import com.example.patrol.SurveyResultsActivity;
import com.example.patrol.service.HttpService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GovtHomeActivity extends AppCompatActivity {

    private String TAG = "Govt Home Activity";
    private HttpService httpService;
    private Gson gson;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_govt_home_page);

        // UI components initialization
        CardView progressBarView = findViewById(R.id.govt_progress_circular_view);
        progressBarView.setVisibility(View.VISIBLE);
        Button buttonProfile = findViewById(R.id.buttonGovtProfile);
        Button buttonHealthStatus = findViewById(R.id.buttonGovtMonitorHealthStatus);
        Button buttonMonitorCrowd = findViewById(R.id.buttonGovtMonitorCrowd);
        Button buttonBroadcasts = findViewById(R.id.buttonGovtBroadcast);
        Button buttonSurveyResults = findViewById(R.id.buttonGovtSurveyResults);

        // Service initialization
        this.httpService = HttpService.getInstance();
        this.gson = new Gson();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {

                String idToken = getTokenResult.getToken();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response = httpService.getUserProfileData(firebaseUser.getEmail(), idToken);
                    User user = gson.fromJson(response.getResponseBody(), User.class);
                    buttonProfile.setOnClickListener(v -> {
                        Log.d(TAG, "Load profile");
                        Intent intent = new Intent(GovtHomeActivity.this, ProfileActivity.class);
                        intent.putExtra("fullName", user.getFullName());
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("role", user.getRole_name());
                        startActivity(intent);
                    });

                });
                executor.shutdown();


                buttonHealthStatus.setOnClickListener(v -> {
                    Log.d(TAG, "Load health status");
                    Intent intent = new Intent(GovtHomeActivity.this, MonitorHealthStatusActivity.class);
                    startActivity(intent);
                });

                buttonMonitorCrowd.setOnClickListener(v -> {
                    Log.d(TAG, "Load crowd monitor");
                    Intent intent = new Intent(GovtHomeActivity.this, MonitorCrowdActivity.class);
                    startActivity(intent);
                });

                buttonBroadcasts.setOnClickListener(v -> {
                    Log.d(TAG, "Load broadcast page");
                    Intent intent = new Intent(GovtHomeActivity.this, BroadcastGovtActivity.class);
                    startActivity(intent);
                });

                buttonSurveyResults.setOnClickListener(v -> {
                    Log.d(TAG, "Load survey page");
                    Intent intent = new Intent(GovtHomeActivity.this, SurveyResultsActivity.class);
                    startActivity(intent);
                });

                progressBarView.setVisibility(View.GONE);

            });
        }



    }
}