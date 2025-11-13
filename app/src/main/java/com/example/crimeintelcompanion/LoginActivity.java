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

public class LoginActivity extends AppCompatActivity {

    private EditText etBadge, etMpin;
    private Button btnLogin;
    private Pref pref;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = new Pref(this);
        etBadge = findViewById(R.id.etBadge);
        etMpin  = findViewById(R.id.etMpin);
        btnLogin = findViewById(R.id.btnLogin);

        if (pref.getBadge() != null) etBadge.setText(pref.getBadge());

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String badge = etBadge.getText().toString().trim().toUpperCase();
        String mpin  = etMpin.getText().toString().trim();

        if (badge.isEmpty() || mpin.length() != 6) {
            Toast.makeText(this, "Enter badge & 6-digit M-PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        pref.setBadge(badge);

        JsonObject body = new JsonObject();
        body.addProperty("IMEI", pref.getImei());
        body.addProperty("badgeNumber", badge);
        body.addProperty("mpin", mpin);

        ApiClient.postJson("login", new Gson().toJson(body), new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials / not active", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
