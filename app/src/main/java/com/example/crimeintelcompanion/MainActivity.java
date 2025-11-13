package com.example.crimeintelcompanion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.crimeintelcompanion.net.ApiClient;
import com.example.crimeintelcompanion.util.Pref;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_READ_PHONE = 101;
    private static final int SPLASH_TIMER = 1500;
    private Pref pref;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = new Pref(this);
        handler.postDelayed(this::checkPermissionAndProceed, SPLASH_TIMER);
    }

    private void checkPermissionAndProceed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQ_READ_PHONE);
        } else {
            verifyImeiWithBackend();
        }
    }

    @SuppressLint("MissingPermission")
    private String getDeviceImeiOrId() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String imei = tm != null ? tm.getDeviceId() : null;
            if (imei == null || imei.trim().isEmpty()) {
                imei = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            return imei;
        } catch (Exception e) {
            return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    private void verifyImeiWithBackend() {
        String imei = getDeviceImeiOrId();
        if (imei == null) {
            goToSetMPIN();
            return;
        }

        pref.setImei(imei);
        JsonObject body = new JsonObject();
        body.addProperty("IMEI", imei);

        ApiClient.postJson("verifyIMEI", new Gson().toJson(body), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    goToSetMPIN();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "{}";
                JsonObject obj = new Gson().fromJson(resp, JsonObject.class);

                boolean exists = obj.has("exists") && obj.get("exists").getAsBoolean();
                String status = obj.has("status") ? obj.get("status").getAsString() : "UNKNOWN";
                boolean hasName = obj.has("hasName") && obj.get("hasName").getAsBoolean();
                boolean hasMpin = obj.has("hasMpin") && obj.get("hasMpin").getAsBoolean();

                runOnUiThread(() -> {
                    if (!exists) {
                        goToSetMPIN(); // ✅ FIXED: IMEI not found -> Set MPIN first
                    }
                    else if (status.equals("APPROVED_PENDING_PROFILE")) {
                        goToSetProfile(); // SHO approved -> complete profile
                    }
                    else if (status.equals("ACTIVE") && hasName && hasMpin) {
                        goToLogin(); // Already registered
                    }
                    else {
                        goToSetProfile(); // fallback for incomplete profile
                    }
                });
            }
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goToSetMPIN() {
        startActivity(new Intent(this, SetMPINActivity.class));
        finish();
    }

    private void goToSetProfile() {
        startActivity(new Intent(this, SetProfileActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        verifyImeiWithBackend();
    }
}
