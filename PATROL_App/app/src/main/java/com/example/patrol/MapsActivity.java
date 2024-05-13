package com.example.patrol;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.widget.SearchView;


import com.example.patrol.DTO.CrowdLocationDTO;
import com.example.patrol.service.CrowdMonitoringService;
import com.example.patrol.service.HttpService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private SearchView searchView;

    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    private Marker searchedLocationMarker;

    private CrowdMonitoringService crowdMonitoringService;

    private HttpService httpService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crowdMonitoringService = CrowdMonitoringService.getInstance();
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        this.httpService = HttpService.getInstance();
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Initialize the SearchView
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search logic here
                Log.d(TAG, "Search Query is " + query);
                //Get the query
                String searchLocation = searchView.getQuery().toString();

                //GeoCode it
                List<Address> addressList = null;
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                try {
                    addressList = geocoder.getFromLocationName(searchLocation, 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Address searchAddress = addressList.get(0);
                Log.d(TAG, "Lat lng " + searchAddress.getLatitude() + " " + searchAddress.getLongitude());

                //Find infection around it
//                List<CrowdLocationDTO> crowdLocationData = crowdMonitoringService.getCrowdLocationData(searchAddress);
                if(searchAddress!=null) plotDataOnMap(searchAddress.getLatitude(), searchAddress.getLongitude(), searchLocation, true);


                //Plot the location, Plot the infection, Plot your current location
//                plotSearchQueryAndCrowdData(searchAddress, searchLocation, crowdLocationData, lastKnownLocation);

                //searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);

    }

    private void plotDataOnMap(double latitude, double longitude, String searchLocation, boolean isSearchQuery) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
                String idToken = getTokenResult.getToken();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ApiResponse response = httpService.getCrowdMapData(latitude, longitude, idToken);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        List<CrowdLocationDTO> crowdLocationDTODeviceList = new ArrayList<>();
                        if (response != null && response.getStatusCode() == 200) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.getResponseBody());
                                Log.d(TAG, "getCrowdLocationDeviceData: " + jsonObject.toString());
                                JSONArray jsonLocationsArray = jsonObject.getJSONArray("locations");
                                Log.d(TAG, "getCrowdLocationDeviceData " + jsonLocationsArray.length());

                                for (int i = 0; i < jsonLocationsArray.length(); i++) {
                                    JSONObject jsonLocationObj = jsonLocationsArray.getJSONObject(i);
                                    double lat = jsonLocationObj.getDouble("latitude");
                                    double lng = jsonLocationObj.getDouble("longitude");
                                    boolean isInfected = jsonLocationObj.getBoolean("isInfected");

                                    CrowdLocationDTO crowdLocationDTO = new CrowdLocationDTO(lat, lng, isInfected);
                                    //Log.d(TAG, crowdLocationDTO.toString());
                                    crowdLocationDTODeviceList.add(crowdLocationDTO);
                                    Log.d(TAG, "crowd1 " + crowdLocationDTODeviceList.size());
                                }
                                if(isSearchQuery) {
                                    plotSearchQueryAndCrowdData(latitude,longitude, searchLocation, crowdLocationDTODeviceList, lastKnownLocation);
                                }
                                else{
                                    plotDeviceLocationAndCrowdData(crowdLocationDTODeviceList);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                }

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

    private void plotSearchQueryAndCrowdData(double latitude, double longitude, String searchLocation, List<CrowdLocationDTO> crowdLocationData, Location lastKnownLocation) {
        Log.d(TAG, ">> plotSearchQueryAndCrowdData "+crowdLocationData.size());
        mMap.clear();
        //plot Searched Address
        LatLng searchLatLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(searchLatLng).title(searchLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, DEFAULT_ZOOM));

        //Plot the infection
        Log.d(TAG, "plotSearchQueryAndCrowdData " + crowdLocationData.size());
        for (CrowdLocationDTO crowdLocationDTO : crowdLocationData) {
            int iconResId = crowdLocationDTO.isInfected() ? R.drawable.red_dot : R.drawable.green_dot;
            addMarkerToMap(crowdLocationDTO.getLatitude(), crowdLocationDTO.getLongitude(), "", iconResId);

        }

        //Plot your current location -- it is always plotted
    }

    private void plotDeviceLocationAndCrowdData(List<CrowdLocationDTO> crowdLocationData) {
        Log.d(TAG, ">> plotDeviceLocationAndCrowdData " + crowdLocationData.size());
        mMap.clear();
        //plot Searched Address

        //Plot the infection
        for (CrowdLocationDTO crowdLocationDTO : crowdLocationData) {
            int iconResId = crowdLocationDTO.isInfected() ? R.drawable.red_dot : R.drawable.green_dot;
            addMarkerToMap(crowdLocationDTO.getLatitude(), crowdLocationDTO.getLongitude(), "", iconResId);

        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    // [START maps_current_place_on_map_ready]
    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap = map;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.

        getDeviceLocation();
//        if (lastKnownLocation != null) {
//            String queryHint = "Current Location: " + lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude();
//            searchView.setQueryHint(queryHint);
//        }
    }
    // [END maps_current_place_on_map_ready]

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null){
                                plotDataOnMap(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),"",false);
                            }else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }


                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        Log.e(TAG, ">> getLocationPermission");

        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
                Log.e(TAG, ">> getLocationPermission Permission Available");

                locationPermissionGranted = true;
                updateLocationUI();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                Log.e(TAG, ">> getLocationPermission Permission Unavailable. Asking for it");

                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                Log.e(TAG, ">> getLocationPermission Asked for it");

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, ">> getLocationPermission Permission Granted after asking");

                    // FCM SDK (and your app) can post notifications.
                    locationPermissionGranted = true;
                    updateLocationUI();
                }
            }
        }
        Log.e(TAG, "<< getLocationPermission ");


    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });
    // [END maps_current_place_location_permission]


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        Log.e(TAG, ">> updateLocationUI " + locationPermissionGranted);

        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                getLocationPermission();
            }
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setPadding(0, 100, 0, 0); //numTop = padding of your choice
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
        Log.e(TAG, "<< updateLocationUI " + locationPermissionGranted);

    }

    private void addMarkerToMap(double latitude, double longitude, String title, int iconResId) {
        LatLng latLng = new LatLng(latitude, longitude);

        // Load the marker icon as a Bitmap
        Drawable drawable = ContextCompat.getDrawable(this, iconResId);
        Bitmap bitmap = drawableToBitmap(drawable);

        // Resize the marker icon
        int width = 30; // Desired width in pixels
        int height = 30; // Desired height in pixels
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        // Create a BitmapDescriptor from the resized Bitmap
        BitmapDescriptor resizedMarkerIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        // Add the marker to the map with the resized icon
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(resizedMarkerIcon); // Set custom icon

        mMap.addMarker(markerOptions);
    }

    // Helper method to convert Drawable to Bitmap
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void searchLocation(String location) {
        // Perform search logic here
        // For simplicity, just add a marker for the searched location
        // Replace this with your actual search logic
        double lat = 34.043013;
        double longitude = -118.267072;
        LatLng searchedLatLng = new LatLng(lat, longitude);
        mMap.addMarker(new MarkerOptions().position(searchedLatLng).title(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 12)); // Zoom to the searched location
    }


}