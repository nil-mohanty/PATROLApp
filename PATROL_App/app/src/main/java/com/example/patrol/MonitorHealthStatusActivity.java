package com.example.patrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.service.HttpService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonitorHealthStatusActivity extends AppCompatActivity {
    private TextView tvTotalUser, tvTotalInfected, tvTotalVaccinated;
    private HttpService httpService;
    private String TAG = "Monitor Health Status Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitor_health_status);

        tvTotalUser = findViewById(R.id.tvTotalUsers);
        tvTotalInfected = findViewById(R.id.tvTotalInfected);
        tvTotalVaccinated = findViewById(R.id.tvTotalVaccinated);
        this.httpService = HttpService.getInstance();
        getHealthRecords();

    }

    private void getHealthRecords(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "getHealthRecords token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.getHeathRecords(idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.getStatusCode() == 200) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response.getResponseBody());
                                int totalNumberOfPeople = jsonResponse.getInt("Total Users");
                                int totalVaccinated = jsonResponse.getInt("Total Vaccinated");
                                int totalInfected = jsonResponse.getInt("Total Infected");
                                tvTotalUser.setText(String.valueOf(totalNumberOfPeople));
                                tvTotalVaccinated.setText(String.valueOf(totalVaccinated));
                                tvTotalInfected.setText(String.valueOf(totalInfected));
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                            }
                        } else {
                            Log.d(TAG, "Failed to get health records : " + response.getResponseBody());
                        }
                    });
                });
                executor.shutdown();

            });
        }

    }
}