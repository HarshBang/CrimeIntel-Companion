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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SetProfileActivity extends AppCompatActivity {

    private EditText etName, etMpin;
    private Button btnSave;
    private Pref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        pref = new Pref(this);

        etName = findViewById(R.id.etName);
        etMpin = findViewById(R.id.etMpin);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String mpin = etMpin.getText().toString().trim();

        if (name.isEmpty() || mpin.length() != 6) {
            Toast.makeText(this, "Enter name & 6-digit M-PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save locally
        pref.setName(name);
        pref.setMpin(mpin);

        JsonObject body = new JsonObject();
        body.addProperty("IMEI", pref.getImei());
        body.addProperty("badgeNumber", pref.getBadge());
        body.addProperty("name", name);
        body.addProperty("mpin", mpin);

        ApiClient.postJson("completeProfile", new Gson().toJson(body), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SetProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                String respBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    if (code == 200) {
                        Toast.makeText(SetProfileActivity.this, "Profile Completed!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(SetProfileActivity.this, HomePageActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        // Show server response for debugging (403 -> Missing Authentication Token)
                        String msg = "Server error (" + code + ")";
                        if (!respBody.isEmpty()) msg += ": " + respBody;
                        Toast.makeText(SetProfileActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
