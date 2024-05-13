package com.example.patrol.BLE;

import static android.content.Context.BLUETOOTH_SERVICE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.patrol.utils.UtilityService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BLEClient {
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private Context context;
    public boolean isEnabled;

    private Map<String, String> bleExchangeCache;

    private UtilityService utilityService;

    private String TAG = "ABBAClient";

    public BLEClient(Context context) {
        this.context = context;
        BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        this.adapter = manager.getAdapter();
        this.scanner = adapter.getBluetoothLeScanner();
        this.isEnabled = false;
        this.utilityService = new UtilityService();
        this.bleExchangeCache = new HashMap<>();
        Log.d(TAG, "BLEClient: Constructor");
    }

    public List<ScanFilter> getScanFilters() {
        UUID BLP_SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
        UUID[] serviceUUIDs = new UUID[]{BLP_SERVICE_UUID};
        List<ScanFilter> filters = null;
        filters = new ArrayList<>();
        Log.d(TAG,">> getScanFilters");

        for (UUID serviceUUID : serviceUUIDs) {
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceData(new ParcelUuid(serviceUUID),utilityService.getServiceDataForClient(),utilityService.getServiceDataMask())
                    .build();
            filters.add(filter);
        }
        Log.d(TAG,"<< getScanFilters");

        return filters;
    }


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();

            if (scanRecord != null) {
                byte[] serviceData = scanRecord.getServiceData(new ParcelUuid(PATROLProfile.PATROL_SERVICE));
                Log.d(TAG, "onScanResult: serviceData bytes " + Arrays.toString(serviceData));
                byte[] encryptedUIDArrayFromServer = utilityService.removeFirstNElements(serviceData,4,20);
                Log.d(TAG, "onScanResult: resultArray bytes " + Arrays.toString(encryptedUIDArrayFromServer));

                //whatever is present make it to hex string
                StringBuilder encryptedUID = utilityService.getHexStringFromBytes(encryptedUIDArrayFromServer);
                Log.d(TAG, "onScanResult  encryptedUID "+ encryptedUID.toString());

                //TODO: Add it to a Map <UUIDHash, LatestTimeSTamp>
                bleExchangeCache.put(encryptedUID.toString(), utilityService.getCurrentTimeStamp());
            }



            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Log.d(TAG, "onScanResult: " + device.getName());
            Log.d(TAG, "onScanResult: " + device.getAddress());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void startScan() {

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        if (scanner != null && checkPermission()) {
//            Intent intent = new Intent(this.context, MyBroadcastReceiver.class);
//            intent.putExtra("o-scan", true);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            scanner.startScan(getScanFilters(), scanSettings, scanCallback);
            this.isEnabled = true;
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
        }
    }


    private boolean checkPermission(){
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    public void stopScan() {
        if (scanner != null) {
            if(!checkPermission()) return;
            scanner.stopScan(scanCallback);
            utilityService.writeBLEDataToFile(this.context, bleExchangeCache);
//            String read = utilityService.readFromFileInternal(this.context);
//            Log.d(TAG, "stopScan: Scanned Stopped " + read);

            bleExchangeCache.clear();
            this.isEnabled = false;
        }
    }
}