package com.example.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.service.HttpService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewBroadcastActivity extends AppCompatActivity {

    private EditText newBroadcastEditText, broadcastTitleEditText;
    private Button submitBroadcastButton;
    private HttpService httpService;
    private FirebaseAuth mAuth;
    private String TAG = "New Broadcast Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_broadcast);
        broadcastTitleEditText = findViewById(R.id.broadcastTitle);
        newBroadcastEditText = findViewById(R.id.newBroadcastEditText);
        submitBroadcastButton = findViewById(R.id.submitBroadcastButton);

        this.httpService = HttpService.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        submitBroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = broadcastTitleEditText.getText().toString().trim();
                String message = newBroadcastEditText.getText().toString().trim();
                sendMessage(user.getEmail(),title, message);
            }
        });
    }

    private void sendMessage(String userEmail, String title, String message) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "sendMessage token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.sendBroadcast(userEmail, title, message, idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.getStatusCode() == 200) {
                            Log.d(TAG, "Broadcast sent! : " + response.getResponseBody());
                            redirectToBroadcastPage("Broadcast sent successfully!");
                        } else {
                            Log.d(TAG, "Broadcast failed: " + response.getResponseBody());
                            showFailedMessage("Broadcast failed! Please try again.");
                        }
                    });
                });
                executor.shutdown();

            });
        }

    }

    private void redirectToBroadcastPage(String message) {
        Intent intent = new Intent(this, BroadcastGovtActivity.class);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showFailedMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}