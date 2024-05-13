package com.example.patrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.patrol.homePage.DevResearcherHomeActivity;
import com.example.patrol.homePage.EcommHomeActivity;
import com.example.patrol.homePage.GenUserHomeActivity;
import com.example.patrol.homePage.GovtHomeActivity;
import com.example.patrol.service.HttpService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button submitButton;
    private FirebaseAuth mAuth;
    private  FirebaseUser currentUser;
    private CardView progressBarView;
    private String TAG = "Login Activity";
    @Override
    public void onStart() {
        super.onStart();
        this.currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        if (intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");
            displayMessage(message);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.progressBarView = findViewById(R.id.login_progress_circular_view);
        this.mAuth = FirebaseAuth.getInstance();
        this.emailEditText = findViewById(R.id.editTextLoginEmailAddress);
        this.passwordEditText = findViewById(R.id.editTextLoginPassword);
        this.submitButton = findViewById(R.id.buttonSubmitLogin);

        this.submitButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(email.isEmpty() || password.isEmpty()) {
                displayMessage("Email and Password cannot be empty");
                return;
            }
            signIn(email, password);
        });
    }

    public void signIn(String email, String password) {
        this.progressBarView.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        user.getIdToken(false).addOnSuccessListener(result -> {
                            Log.d(TAG, "signIn: " + result.getToken());
                            Map<String, Object> claims = result.getClaims();
                            launchActivityBasedOnRole(claims);
                        });

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Invalid Credentials",
                                Toast.LENGTH_SHORT).show();
                        progressBarView.setVisibility(View.GONE);
                    }
                });
    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void launchActivityBasedOnRole(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            Log.d(TAG, "No claims found");
            return;
        }
        Intent intent = new Intent();
        for (String key : claims.keySet()) {
            if (key.equals("GEN") && (boolean) claims.get(key)) {
                intent = new Intent(LoginActivity.this, GenUserHomeActivity.class);
            } else if (key.equals("GOVT") && (boolean) claims.get(key)) {
                intent = new Intent(LoginActivity.this, GovtHomeActivity.class);
            } else if (key.equals("ECOMM") && (boolean) claims.get(key)) {
                intent = new Intent(LoginActivity.this, EcommHomeActivity.class);
            } else if (key.equals("RES") && (boolean) claims.get(key)) {
                intent = new Intent(LoginActivity.this, DevResearcherHomeActivity.class);
            }
        }
        startActivity(intent);
        progressBarView.setVisibility(View.GONE);
        finish();
    }

}