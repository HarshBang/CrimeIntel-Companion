package com.example.crimeintelcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.crimeintelcompanion.net.ApiClient;
import com.example.crimeintelcompanion.util.Pref;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VerifyBadgeActivity extends AppCompatActivity {

    private EditText etBadge;
    private Button btnVerify;
    private Pref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_badge);

        etBadge = findViewById(R.id.etBadge);
        btnVerify = findViewById(R.id.btnVerify);
        pref = new Pref(this);

        btnVerify.setOnClickListener(v -> verifyBadge());
    }

    // -------------------------------------------------------
    // STEP 1: VERIFY BADGE FROM BACKEND
    // -------------------------------------------------------
    private void verifyBadge() {
        String badge = etBadge.getText().toString().trim().toUpperCase();

        if (badge.isEmpty()) {
            Toast.makeText(this, "Enter your Badge Number", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("badgeNumber", badge);
        body.addProperty("IMEI", pref.getImei());  // needed to detect mismatch

        ApiClient.postJson("verifyBadge", new Gson().toJson(body), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(
                        VerifyBadgeActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT
                ).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resp = response.body() != null ? response.body().string() : "{}";
                JsonObject json = new Gson().fromJson(resp, JsonObject.class);

                runOnUiThread(() -> {
                    boolean exists = json.has("exists") && json.get("exists").getAsBoolean();
                    boolean hasImei = json.has("hasImei") && json.get("hasImei").getAsBoolean();
                    boolean imeiMatches = json.has("imeiMatches") && json.get("imeiMatches").getAsBoolean();

                    if (!exists) {
                        Toast.makeText(VerifyBadgeActivity.this, "Badge not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pref.setBadge(badge);

                    // CASE 1: Badge exists but *no Device registered* → FIRST TIME LOGIN
                    if (!hasImei) {
                        associateDevice(badge);
                    }

                    // CASE 2: Badge exists & IMEI matches → LOGIN PAGE
                    else if (imeiMatches) {
                        Intent i = new Intent(VerifyBadgeActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }

                    // CASE 3: Badge exists but IMEI DOES NOT match → NEW PHONE → MANUAL APPROVAL
                    else {
                        Intent i = new Intent(VerifyBadgeActivity.this, ManualVerificationActivity.class);
                        i.putExtra("badgeNumber", badge);
                        startActivity(i);
                        finish();
                    }
                });
            }
        });
    }

    // -------------------------------------------------------
    // STEP 2: FIRST-TIME REGISTER DEVICE (IMEI + MPIN + NAME)
    // -------------------------------------------------------
    private void associateDevice(String badge) {

        JsonObject body = new JsonObject();
        body.addProperty("IMEI", pref.getImei());
        body.addProperty("badgeNumber", badge);
        body.addProperty("mpin", pref.getMpin());             // stored earlier
        body.addProperty("name", pref.getName() == null ? "" : pref.getName());

        ApiClient.postJson("associateDevice", new Gson().toJson(body), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(
                        VerifyBadgeActivity.this,
                        "Network error during registration",
                        Toast.LENGTH_SHORT
                ).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            VerifyBadgeActivity.this,
                            "Device Registered Successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    startActivity(new Intent(VerifyBadgeActivity.this, HomePageActivity.class));
                    finish();
                });
            }
        });
    }
}
