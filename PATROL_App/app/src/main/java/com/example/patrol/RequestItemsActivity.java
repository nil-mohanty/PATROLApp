package com.example.patrol;

import static com.example.patrol.R.*;
import static com.example.patrol.R.layout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.patrol.service.HttpService;
import com.example.patrol.utils.ItemAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestItemsActivity extends AppCompatActivity {
    private static final String TAG = "RequestItemsActivity";
    private SeekBar seekBarBread, seekBarMask, seekBarSanitizer, seekBarTissuePaper;
    private TextView textViewValueBread, textViewValueMask, textViewValueSanitizer, textViewValueTissuePaper;
    private Button buttonSave;
    private Spinner requestItemsAreaDropdown;
    private HttpService httpService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.request_items);

        seekBarBread = findViewById(R.id.seekBarCounterBread);
        seekBarMask = findViewById(R.id.seekBarCounterMask);
        seekBarSanitizer = findViewById(R.id.seekBarCounterSanitizer);
        seekBarTissuePaper = findViewById(R.id.seekBarCounterTissuePaper);

        textViewValueBread = findViewById(R.id.textViewCounterValueBread);
        textViewValueMask = findViewById(R.id.textViewCounterValueMask);
        textViewValueSanitizer = findViewById(R.id.textViewCounterValueSanitizer);
        textViewValueTissuePaper = findViewById(R.id.textViewCounterValueTissuePaper);
        requestItemsAreaDropdown = findViewById(id.dropDownArea);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.area_dropdown,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        requestItemsAreaDropdown.setAdapter(adapter);

        seekBarBread.setMax(10);
        seekBarMask.setMax(10);
        seekBarSanitizer.setMax(10);
        seekBarTissuePaper.setMax(10);

        seekBarBread.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewValueBread.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarMask.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewValueMask.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarSanitizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewValueSanitizer.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarTissuePaper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewValueTissuePaper.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        buttonSave = findViewById(R.id.buttonItemsSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value1 = seekBarBread.getProgress();
                int value2 = seekBarMask.getProgress();
                int value3 = seekBarSanitizer.getProgress();
                int value4 = seekBarTissuePaper.getProgress();

                String selectedArea = requestItemsAreaDropdown.getSelectedItem().toString();
                Map<String,Integer> itemsMap = new HashMap<>();
                itemsMap.put("Bread",value1);
                itemsMap.put("Sanitizer",value2);
                itemsMap.put("Mask",value3);
                itemsMap.put("Tissue Paper",value4);

                sendReorderedItems(itemsMap,selectedArea);
            }
        });
        this.httpService = HttpService.getInstance();

    }

    private void sendReorderedItems(Map<String,Integer> itemsMap, String selectedArea) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                Log.d(TAG, "sendReorderedItems token: " + idToken);
                ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.execute(() -> {
                    String response = this.httpService.sendRequestItems(itemsMap,selectedArea,idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(RequestItemsActivity.this, response, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
                executor.shutdown();
            });
        }
    }
}

