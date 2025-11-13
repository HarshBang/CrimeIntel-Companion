package com.example.crimeintelcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
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

public class PendingApprovalActivity extends AppCompatActivity {

    private Pref pref;
    private Handler handler = new Handler();
    private boolean stopped = false;
    private static final int CHECK_INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approval);

        pref = new Pref(this);

        TextView tv = findViewById(R.id.textStatus);
        tv.setText("Waiting for SHO approval...");

        startCheckingStatus();
    }

    private void startCheckingStatus() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (stopped) return;
                checkApprovalStatus();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }, 2000);
    }

    private void checkApprovalStatus() {
        JsonObject body = new JsonObject();
        body.addProperty("badgeNumber", pref.getBadge());
        body.addProperty("IMEI", pref.getImei());

        ApiClient.postJson("verifyBadge", new Gson().toJson(body), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PendingApprovalActivity.this, "Network error…", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "{}";
                System.out.println("📩 verifyBadge RESPONSE → " + resp);

                JsonObject json = new Gson().fromJson(resp, JsonObject.class);

                boolean exists = json.has("exists") && json.get("exists").getAsBoolean();
                boolean imeiMatches = json.has("imeiMatches") && json.get("imeiMatches").getAsBoolean();
                String status = json.has("status") && !json.get("status").isJsonNull()
                        ? json.get("status").getAsString().trim()
                        : "UNKNOWN";
                boolean hasName = json.has("name") && !json.get("name").isJsonNull()
                        && !json.get("name").getAsString().trim().isEmpty();
                boolean hasMpin = json.has("hasMpin") && json.get("hasMpin").getAsBoolean();

                System.out.println("🧠 STATUS=" + status + " IMEI_MATCH=" + imeiMatches + " HAS_NAME=" + hasName + " HAS_MPIN=" + hasMpin);

                if (!exists) return;

                // ✅ CASE 1: SHO has approved but user hasn’t completed profile
                if ("APPROVED_PENDING_PROFILE".equals(status) ||
                        ("ACTIVE".equals(status) && !hasMpin)) {

                    stopped = true;
                    handler.removeCallbacksAndMessages(null);
                    runOnUiThread(() -> {
                        Toast.makeText(PendingApprovalActivity.this, "Approved! Proceed to Profile Setup.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PendingApprovalActivity.this, SetProfileActivity.class));
                        finish();
                    });
                    return;
                }

                // ✅ CASE 2: Profile fully active and has MPIN — go to login
                if ("ACTIVE".equals(status) && hasMpin) {
                    stopped = true;
                    handler.removeCallbacksAndMessages(null);
                    runOnUiThread(() -> {
                        Toast.makeText(PendingApprovalActivity.this, "Profile Active. Proceed to Login.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PendingApprovalActivity.this, LoginActivity.class));
                        finish();
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stopped = true;
    }
}
