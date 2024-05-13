package com.example.patrol.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UtilityService {
    private static final String TAG = "Utils";

    //Returns byte array of SHA-1 encryption of randomly generated UID
    public byte[] getServiceData(String uuid) {
        //TODO
        //Get Random UUID
        Log.d(TAG, "getServiceData no args");

        Log.d(TAG, "getServiceData UID " + uuid);

        //Encrypt the UID
        byte[] encryptedUID;
        try {
            encryptedUID = encryptBySHA1(uuid);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return getServiceData(encryptedUID);
    }

    //    getServiceDataForClient : Returns a dummy service data in bytes
//    with the first 4 elements as "UID-" so that the same can be used as
//    reference for checking servicedata from server
    public byte[] getServiceDataForClient() {
        Log.d(TAG, " >> getServiceDataForClient");
        byte[] prefix = "UID-".getBytes();
        byte[] byteArray = new byte[20];
        Log.d(TAG, " << getServiceDataForClient");
        return mergeByteArrays(prefix, byteArray);


    }

    public byte[] getServiceData(byte[] data) {
        //TODO
        byte[] prefix = "UID-".getBytes();
        Log.d(TAG, "getServiceData " + "UID- " + Arrays.toString(prefix));
        Log.d(TAG, "getServiceDataInBytes " + Arrays.toString(("UID-" + data).getBytes()));

        return mergeByteArrays(prefix, data);
    }

    //Encrypt by sha1
    public byte[] encryptBySHA1(String text) throws NoSuchAlgorithmException {
        //TODO
        Log.d(TAG, ">> encryptBySHA1 text" + text);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        Log.d(TAG, ">> encryptBySHA1 digest " + Arrays.toString(digest));
        return digest;
    }

    //Generate Random UID
    public String generateRandomUID() {
        return UUID.randomUUID().toString();
    }

    public byte[] getServiceDataMask() {
        return new byte[]{1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    }

    private static byte[] mergeByteArrays(byte[] array1, byte[] array2) {
        byte[] mergedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        Log.d(TAG, "<< mergeByteArrays " + Arrays.toString(mergedArray));
        return mergedArray;
    }

    public byte[] removeFirstNElements(byte[] serviceData, int startPos, int newLength) {
        byte[] resultArray = new byte[newLength];
        System.arraycopy(serviceData, startPos, resultArray, 0, newLength);
        return resultArray;
    }

    public StringBuilder getHexStringFromBytes(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : array) {
            hexString.append(String.format("%02x", b));
        }
        return hexString;
    }

    public boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    public String getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getEmail();
        } else return "DUMMY";

    }

    public String parseTimestamp(String timestamp){
        String formattedTimestamp = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = inputFormat.parse(timestamp);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            formattedTimestamp = outputFormat.format(date);
            return formattedTimestamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTimestamp;
    }

    public String getCurrentTimeStamp(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return formatter.format(now);
    }

    public void writeBLEDataToFile(Context context, Map<String, String> exchangeHistory) {
        if(exchangeHistory.isEmpty()) return;
        try {
            // Open a private file associated with this Context's application package for writing.
            FileOutputStream fos = context.openFileOutput("ble_history.txt", Context.MODE_APPEND);
            StringBuilder sb = new StringBuilder();
            for(String key : exchangeHistory.keySet()) {
                if(!exchangeHistory.get(key).isEmpty()) {
                    sb.append(key);
                    sb.append(",");
                    sb.append(exchangeHistory.get(key));
                    sb.append("\n");
                }

            }
            fos.write(sb.toString().getBytes());
            fos.close();
            Log.d("File Write", "File written successfully");
        } catch (IOException e) {
            Log.e("File Write Error", "Error writing to file", e);
        }
    }

    public String readFromFileInternal(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput("ble_history.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            fis.close();
        } catch (IOException e) {
            Log.e("File Read Error", "Error reading from file", e);
        }
        return stringBuilder.toString();
    }



}