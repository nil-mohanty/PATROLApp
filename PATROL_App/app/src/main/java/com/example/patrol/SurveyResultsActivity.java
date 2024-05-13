package com.example.patrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patrol.DTO.DemandItem;
import com.example.patrol.enums.CityCoordinates;
import com.example.patrol.service.HttpService;
import com.example.patrol.utils.DemandAdapter;
import com.example.patrol.utils.TextAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SurveyResultsActivity extends AppCompatActivity {

    private String TAG = "Survey Results Activity";
    LinearLayout yesterdayTrendTable, weekTrendTable, monthTrendTable;
    private Spinner areaDropDown;
    private Button buttonSubmit;
    private HttpService httpService;
    private RecyclerView recyclerViewPastMonth, recyclerViewPastWeek, recyclerViewYesterday;
    private DemandAdapter pastMonthAdapter, pastWeekAdapter, yesterdayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey_results);

        areaDropDown = findViewById(R.id.dropDownArea);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        recyclerViewPastMonth = findViewById(R.id.recyclerViewPastMonth);
        recyclerViewPastWeek = findViewById(R.id.recyclerViewPastWeek);
        recyclerViewYesterday = findViewById(R.id.recyclerViewYesterday);
        yesterdayTrendTable = findViewById(R.id.yesterdayTrendTable);
        weekTrendTable = findViewById(R.id.weekTrendTable);
        monthTrendTable = findViewById(R.id.monthTrendTable);
        this.httpService = HttpService.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.area_dropdown, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaDropDown.setAdapter(adapter);

        buttonSubmit.setOnClickListener(v -> onSubmitClicked());
    }

    private void onSubmitClicked() {
        yesterdayTrendTable.setVisibility(View.VISIBLE);
        weekTrendTable.setVisibility(View.VISIBLE);
        monthTrendTable.setVisibility(View.VISIBLE);

        String selectedCity = areaDropDown.getSelectedItem().toString();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "onSubmitClicked token: " + idToken);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response =  this.httpService.getSupplyDemand(selectedCity, idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.getStatusCode() == 200) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response.getResponseBody());

                                //Parse "Past Month Demand" data
                                JSONArray pastMonthArray = jsonResponse.getJSONObject("Past Month Demand").names();
                                List<DemandItem> pastMonthDemand = parseDemandItems(pastMonthArray, jsonResponse.getJSONObject("Past Month Demand"));

                                // Parse "Past Week Demand" data
                                JSONArray pastWeekArray = jsonResponse.getJSONObject("Past Week Demand").names();
                                List<DemandItem> pastWeekDemand = parseDemandItems(pastWeekArray, jsonResponse.getJSONObject("Past Week Demand"));

                                // Parse "Yesterday Demand" data
                                JSONArray yesterdayArray = jsonResponse.getJSONObject("Yesterday Demand").names();
                                List<DemandItem> yesterdayDemand = parseDemandItems(yesterdayArray, jsonResponse.getJSONObject("Yesterday Demand"));

                                pastMonthAdapter = new DemandAdapter(pastMonthDemand);
                                pastWeekAdapter = new DemandAdapter(pastWeekDemand);
                                yesterdayAdapter = new DemandAdapter(yesterdayDemand);

                                recyclerViewPastMonth.setAdapter(pastMonthAdapter);
                                recyclerViewPastWeek.setAdapter(pastWeekAdapter);
                                recyclerViewYesterday.setAdapter(yesterdayAdapter);

                                recyclerViewPastMonth.setLayoutManager(new LinearLayoutManager(this));
                                recyclerViewPastWeek.setLayoutManager(new LinearLayoutManager(this));
                                recyclerViewYesterday.setLayoutManager(new LinearLayoutManager(this));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            pastMonthAdapter.notifyDataSetChanged();
                            pastWeekAdapter.notifyDataSetChanged();
                            yesterdayAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Failed to get results : " + response.getResponseBody());
                        }
                    });
                });
                executor.shutdown();
            });
        }
    }

    private List<DemandItem> parseDemandItems(JSONArray jsonArray, JSONObject jsonObject) throws JSONException {
        List<DemandItem> demandItems = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String itemName = jsonArray.getString(i);
                int quantity = jsonObject.getInt(itemName);
                demandItems.add(new DemandItem(itemName, quantity));
            }
        }
        return demandItems;
    }
}