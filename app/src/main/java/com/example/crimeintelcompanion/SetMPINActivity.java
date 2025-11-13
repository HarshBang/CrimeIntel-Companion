package com.example.crimeintelcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.crimeintelcompanion.util.Pref;

public class SetMPINActivity extends AppCompatActivity {

    private EditText etMpin, etConfirmMpin;
    private Button btnSave;
    private Pref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_mpin);

        pref = new Pref(this);
        etMpin = findViewById(R.id.etMpin);
        etConfirmMpin = findViewById(R.id.etConfirmMpin);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveMpin());
    }

    private void saveMpin() {
        String mpin = etMpin.getText().toString().trim();
        String confirm = etConfirmMpin.getText().toString().trim();

        // Basic validation
        if (mpin.length() != 6) {
            Toast.makeText(this, "Enter a 6-digit MPIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mpin.equals(confirm)) {
            Toast.makeText(this, "MPINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save MPIN locally (not in database yet)
        pref.setMpin(mpin);

        Toast.makeText(this, "MPIN set successfully!", Toast.LENGTH_SHORT).show();

        // Go to Verify Badge next
        Intent intent = new Intent(SetMPINActivity.this, VerifyBadgeActivity.class);
        startActivity(intent);
        finish();
    }
}
