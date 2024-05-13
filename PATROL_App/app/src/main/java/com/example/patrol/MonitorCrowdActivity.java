package com.example.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.homePage.GenUserHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MonitorCrowdActivity extends AppCompatActivity {
    private Button buttonSearchMaps;
    private Button buttonTrends;
    private String TAG = "Crowd Monitoring Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crowd_monitoring_landing_page);

        buttonSearchMaps = findViewById(R.id.buttonSearchMaps);
        buttonTrends = findViewById(R.id.buttonTrends);

        buttonSearchMaps.setOnClickListener(v -> {
            Log.d(TAG, "Start Seaching maps ");
            Intent intent = new Intent(MonitorCrowdActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        buttonTrends.setOnClickListener(v -> {
            Log.d(TAG, "Check trends ");
            Intent intent = new Intent(MonitorCrowdActivity.this, GeneralTrendsActivity.class);
            startActivity(intent);
        });

    }

}