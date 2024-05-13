package com.example.patrol.BLE;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class BLEJobService extends Service {

    BLEClient client;
    BLEServer server;

    public static String TAG = "BGGGG";
    private String uuid;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uuid = intent.getStringExtra("uuid");
        Log.d(TAG, "onStartCommand: " + uuid);
        this.client = new BLEClient(this);
        this.server = new BLEServer(this, uuid);
        Toast.makeText(
                getApplicationContext(), "BLE Service has started running in the background",
                Toast.LENGTH_SHORT
        ).show();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Code to be executed periodically
                if(client.isEnabled) {
                    client.stopScan();
                }
                else {
                    client.startScan();
                }
            }
        };
        int delay = 1000; // Delay in milliseconds (1 second)
        int interval = 2 *30 * 1000; // Interval in milliseconds (5 mins)
        timer.scheduleAtFixedRate(timerTask, delay, interval);

        server.startAdvertising();

        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d("Stopping", "Stopping Service");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(
                getApplicationContext(), "Service execution completed",
                Toast.LENGTH_SHORT
        ).show();
        Log.d("Stopped", "Service Stopped");
        super.onDestroy();
    }

}