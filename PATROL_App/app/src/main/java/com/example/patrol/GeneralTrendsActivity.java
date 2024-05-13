package com.example.patrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.patrol.enums.CityCoordinates;
import com.example.patrol.service.HttpService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GeneralTrendsActivity extends AppCompatActivity {
    private Spinner dropDownArea;
    private TextView textViewCounterValue;
    private TextView tvTotalPeople;
    private TextView tvInfectedPeople;
    private Button buttonMinus;
    private Button buttonPlus;
    private Button buttonSubmit;
    private HttpService httpService;
    private int counterValue;
    private String TAG = "General Trends Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_trends);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dropDownArea = findViewById(R.id.dropDownArea);
        textViewCounterValue = findViewById(R.id.textViewCounterValue);
        tvTotalPeople = findViewById(R.id.tvTotalPeople);
        tvInfectedPeople = findViewById(R.id.tvInfectedPeople);
        buttonMinus = findViewById(R.id.buttonMinus);
        buttonPlus = findViewById(R.id.buttonPlus);
        buttonSubmit = findViewById(R.id.buttonTrendsSubmit);
        this.httpService = HttpService.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.area_dropdown, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDownArea.setAdapter(adapter);

        counterValue = 0;
        buttonMinus.setOnClickListener(v -> {
            if (counterValue > -10) {
                counterValue--;
                textViewCounterValue.setText(String.valueOf(counterValue));
            }
        });

        buttonPlus.setOnClickListener(v -> {
            if (counterValue < 10) {
                counterValue++;
                textViewCounterValue.setText(String.valueOf(counterValue));
            }
        });

        buttonSubmit.setOnClickListener(v -> onSubmitClicked());
    }

    private void onSubmitClicked() {
        String selectedCity = dropDownArea.getSelectedItem().toString();
        CityCoordinates cityCoordinates = CityCoordinates.fromString(selectedCity);
        String latitude = String.valueOf(cityCoordinates.getLatitude());
        String longitude = String.valueOf(cityCoordinates.getLongitude());

        String numberOfDays = textViewCounterValue.getText().toString();
        Log.d(TAG, latitude +" , "+ longitude + " , "+ numberOfDays);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "onSubmitClicked token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.getTrends(latitude, longitude, numberOfDays, idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.getStatusCode() == 200) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response.getResponseBody());
                                int totalNumberOfPeople = jsonResponse.getInt("totalNumberOfPeople");
                                int totalInfected = jsonResponse.getInt("totalInfected");

                                tvTotalPeople.setText(String.valueOf(totalNumberOfPeople));
                                tvInfectedPeople.setText(String.valueOf(totalInfected));
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                            }
                        } else {
                            Log.d(TAG, "Failed to get trends : " + response.getResponseBody());
                        }
                    });
                });
                executor.shutdown();

            });
        }

    }
}
