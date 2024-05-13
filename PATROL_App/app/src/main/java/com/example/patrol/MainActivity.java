package com.example.patrol;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.patrol.BLE.BLEJobService;
import com.example.patrol.homePage.EcommHomeActivity;
import com.example.patrol.homePage.GenUserHomeActivity;
import com.example.patrol.homePage.GovtHomeActivity;
import com.example.patrol.service.LocationHeartbeatService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private String[] PERMISSIONS;

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void askReadWritePermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void askLocationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PERMISSIONS = new String[] {

                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE

        };

        if (!hasPermissions(MainActivity.this,PERMISSIONS)) {

            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS,1);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                Intent permission = new Intent();
                permission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(permission);
            }
        }
        //askPermissions();
//        Log.d(TAG,"Asking Notification Permission");
//        askNotificationPermission();
//        Log.d(TAG,"Asking location Permission");
//        askLocationPermission();
//        askReadWritePermission();

        // Subscribing to a topic with an onComplete listener
        FirebaseMessaging.getInstance().subscribeToTopic("send_broadcast")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to broadcast channel";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d(TAG, msg);
                });

        FirebaseMessaging.getInstance().subscribeToTopic("exposure")
                .addOnCompleteListener( task -> {
                    String msg = "Subscribed to exposure channel";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d(TAG, msg);
                });

        // Getting the Firebase Notification registration token. This is required to be stored in database
        Log.d(TAG, "onCreate: " + FirebaseMessaging.getInstance().getToken());

        // Check if user already has an active session, then route to respective home page
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            user.getIdToken(false).addOnSuccessListener(result -> {
                Log.d(TAG, "signIn: " + result.getToken());
                Map<String, Object> claims = result.getClaims();
                launchActivityBasedOnRole(claims);
            });
        }


        // Starting the Registration Activity on button click
        Button registerButton = findViewById(R.id.buttonSignUp);
        registerButton.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, RegistrationActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        Button loginButton = findViewById(R.id.buttonSignIn);
        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Calling Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(this, "Calling Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "SMS Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
               // Toast.makeText(this, "SMS Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Camera Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(this, "Camera Permission is denied", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Camera Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(this, "Camera Permission is denied", Toast.LENGTH_SHORT).show();
            }



        }
    }
    private boolean hasPermissions(Context context, String... PERMISSIONS) {

        if (context != null && PERMISSIONS != null) {

            for (String permission: PERMISSIONS){

                if (ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }
    private void askPermissions() {
        // Request POST_NOTIFICATIONS permission
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

        // Request ACCESS_FINE_LOCATION permission
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        // Request WRITE_EXTERNAL_STORAGE permission
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void launchActivityBasedOnRole(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            Log.d(TAG, "No claims found");
            return;
        }
        Intent intent = new Intent();
        for (String key : claims.keySet()) {
            if (key.equals("GEN") && (boolean) claims.get(key)) {
                intent = new Intent(MainActivity.this, GenUserHomeActivity.class);
            } else if (key.equals("GOVT") && (boolean) claims.get(key)) {
                intent = new Intent(MainActivity.this, GovtHomeActivity.class);
            } else if (key.equals("ECOMM") && (boolean) claims.get(key)) {
                intent = new Intent(MainActivity.this, EcommHomeActivity.class);
            } else if (key.equals("RES") && (boolean) claims.get(key)) {
//                intent = new Intent(LoginActivity.this, EcommHomeActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }
}