package com.example.crimeintelcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class ManualVerificationActivity extends AppCompatActivity {

    private EditText etName, etBadgeNumber, etImei;
    private Button btnSubmit;
    private Pref pref;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_verification);

        etName = findViewById(R.id.etName);
        etBadgeNumber = findViewById(R.id.etBadgeNumber);
        etImei = findViewById(R.id.etImei);
        btnSubmit = findViewById(R.id.btnSubmit);

        pref = new Pref(this);

        String badge = getIntent().getStringExtra("badgeNumber");
        if (badge != null) etBadgeNumber.setText(badge);
        etImei.setText(pref.getImei());

        btnSubmit.setOnClickListener(v -> submitManualVerification());
    }

    private void submitManualVerification() {
        String name  = etName.getText().toString().trim();
        String badge = etBadgeNumber.getText().toString().trim().toUpperCase();
        String imei  = etImei.getText().toString().trim();

        if (name.isEmpty() || badge.isEmpty() || imei.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("IMEI", imei);
        body.addProperty("badgeNumber", badge);
        body.addProperty("name", name);

        ApiClient.postJson("requestManualVerification", new Gson().toJson(body), new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ManualVerificationActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ManualVerificationActivity.this,
                                "Submitted. Waiting for SHO approval…", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ManualVerificationActivity.this, PendingApprovalActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ManualVerificationActivity.this,
                                "Failed to submit (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
