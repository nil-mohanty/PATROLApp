package com.example.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.homePage.GenUserHomeActivity;
import com.example.patrol.service.HttpService;
import com.example.patrol.utils.UtilityService;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateHealthStatusActivity extends AppCompatActivity {
    private RadioGroup rgStatus;
    private EditText tilSymptoms;
    private DatePicker dateVaccination;
    private FirebaseAuth mAuth;
    private HttpService httpService;
    private UtilityService utilityService;
    private Button buttonSave;
    private String TAG = "Health Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_health_status);

        rgStatus = findViewById(R.id.rgStatus);
        tilSymptoms = findViewById(R.id.tvSymptoms);
        dateVaccination = findViewById(R.id.dateVaccination);
        buttonSave = findViewById(R.id.buttonSave);
        this.httpService = HttpService.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.utilityService = new UtilityService();
        String userEmail = mAuth.getCurrentUser().getEmail();
        tilSymptoms.setVisibility(View.GONE);
        dateVaccination.setVisibility(View.GONE);

        rgStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbInfected) {
                    tilSymptoms.setVisibility(View.VISIBLE);
                    dateVaccination.setVisibility(View.GONE);
                } else if (checkedId == R.id.rbVaccinated) {
                    tilSymptoms.setVisibility(View.GONE);
                    dateVaccination.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonSave.setOnClickListener(v -> {
            int selectedId = rgStatus.getCheckedRadioButtonId();

            if (selectedId == R.id.rbInfected) {
                String symptoms = tilSymptoms.getText().toString().trim();
                if (symptoms.isEmpty()) {
                    tilSymptoms.setError("Please enter symptoms");
                    return;
                }
                updateUserInfection(userEmail, true, symptoms);
            } else if (selectedId == R.id.rbVaccinated) {
                updateUserVaccination(userEmail, dateVaccination.getDayOfMonth(), dateVaccination.getMonth(), dateVaccination.getYear());
            }
        });

    }

    private void updateUserInfection(String userEmail, boolean isInfected, String symptoms){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "updateUserInfection token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                List<String> bleHistory = readBleHistoryFromFile();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.updateUserInfection(userEmail, isInfected, bleHistory, symptoms, idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.getStatusCode() == 201) {
                            Log.d(TAG, "Health record updated successfully: " + response.getResponseBody());
                            Toast.makeText(UpdateHealthStatusActivity.this, "Health record updated successfully", Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            Log.d(TAG, "Health record updation failed " + response.getResponseBody());
                            Toast.makeText(UpdateHealthStatusActivity.this, "Health record updation failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
                executor.shutdown();
            });
        }
    }

    private void updateUserVaccination(String userEmail, int day, int month, int year){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "updateUserVaccination token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.updateUserVaccination(userEmail, day, month, year, idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.getStatusCode() == 201) {
                            Log.d(TAG, "Health record updated successfully: " + response.getResponseBody());
                            Toast.makeText(UpdateHealthStatusActivity.this, "Health record updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.d(TAG, "Health record updation failed " + response.getResponseBody());
                            Toast.makeText(UpdateHealthStatusActivity.this, "Health record updation failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
                executor.shutdown();
            });
        }
    }

    private List<String> readBleHistoryFromFile() {
        String fileData = utilityService.readFromFileInternal(this);
        if(fileData.isEmpty()) return Collections.emptyList();
        Log.d(TAG, "readBleHistoryFromFile: " + fileData);
        Map<String, String> recordsMap = new HashMap<>();
        for(String line : fileData.split("\n")) {
            String[] entries = line.split(",");
            recordsMap.put(entries[0], entries[1]);
        }

        List<String> bleHistory = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(String key : recordsMap.keySet()) {
            sb.append(key);
            sb.append(",");
            sb.append(recordsMap.get(key));
            bleHistory.add(sb.toString());
            sb.setLength(0);
        }

        return bleHistory;
    }
    private void redirectToHomePage(String message) {
        Intent intent = new Intent(this, GenUserHomeActivity.class);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showFailedMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}