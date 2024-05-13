package com.example.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patrol.service.HttpService;
import com.example.patrol.utils.TextAdapter;
import com.example.patrol.utils.UtilityService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BroadcastGovtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextAdapter adapter;
    private HttpService httpService;
    private UtilityService utilityService;
    private List<BroadcastItem> broadcastMessages = new ArrayList<>();
    private String TAG = "Broadcast Govt Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_govt_display);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.httpService = HttpService.getInstance();
        this.utilityService = new UtilityService();
        loadBroadcastData();
        adapter = new TextAdapter(broadcastMessages);
        recyclerView.setAdapter(adapter);

        Button newBroadcastButton = findViewById(R.id.buttonNewBroadcast);
        newBroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BroadcastGovtActivity.this, NewBroadcastActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadBroadcastData(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "loadBroadcastData token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.getBroadcastMessages(idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response != null && response.getStatusCode() == 200) {
                            try {
                                JSONArray jsonArray = new JSONArray(response.getResponseBody());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    int messageId = jsonObject.getInt("message_id");
                                    String title = jsonObject.getString("title");
                                    String message = jsonObject.getString("message");
                                    String timestamp = utilityService.parseTimestamp(jsonObject.getString("timestamp"));

                                    BroadcastItem broadcastMessage = new BroadcastItem(messageId, timestamp, title, message);
                                    Log.d(TAG, broadcastMessage.toString());
                                    broadcastMessages.add(broadcastMessage);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "Invalid or empty response");
                        }
                    });
                });
                executor.shutdown();

            });
        }

    }

}
