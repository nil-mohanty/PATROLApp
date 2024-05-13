package com.example.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.DTO.User;
import com.example.patrol.service.HttpService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvFullName, tvEmail, tvRole;
    private Button buttonSignOut;
    private HttpService httpService;
    private FirebaseAuth mAuth;
    private Gson gson = new Gson();
    private String TAG = "Profile Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");

        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        this.httpService = HttpService.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        this.tvFullName.setText(fullName);
        this.tvEmail.setText(email);
        this.tvRole.setText(role);

        buttonSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}