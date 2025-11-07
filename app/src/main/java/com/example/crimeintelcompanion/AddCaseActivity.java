package com.example.crimeintelcompanion;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class AddCaseActivity extends AppCompatActivity {

    EditText inputFirNumber, inputCaseTitle, inputPoliceStation, inputLatitude, inputLongitude,
            inputCrimeAddress, inputComplainantName, inputComplainantContact, inputIncidentSummary;
    Spinner spinnerCaseType;
    Button btnCreateCase;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_case);

        dbHelper = new DatabaseHelper(this);

        inputFirNumber = findViewById(R.id.inputFirNumber);
        inputCaseTitle = findViewById(R.id.inputCaseTitle);
        spinnerCaseType = findViewById(R.id.spinnerCaseType);
        inputPoliceStation = findViewById(R.id.inputPoliceStation);
        inputLatitude = findViewById(R.id.inputLatitude);
        inputLongitude = findViewById(R.id.inputLongitude);
        inputCrimeAddress = findViewById(R.id.inputCrimeAddress);
        inputComplainantName = findViewById(R.id.inputComplainantName);
        inputComplainantContact = findViewById(R.id.inputComplainantContact);
        inputIncidentSummary = findViewById(R.id.inputIncidentSummary);
        btnCreateCase = findViewById(R.id.btnCreateCase);

        btnCreateCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap<String, String> data = new HashMap<>();
                data.put("fir_number", inputFirNumber.getText().toString());
                data.put("title", inputCaseTitle.getText().toString());
                data.put("type", spinnerCaseType.getSelectedItem() != null ? spinnerCaseType.getSelectedItem().toString() : "");
                data.put("status", "Active");
                data.put("police_station", inputPoliceStation.getText().toString());
                data.put("latitude", inputLatitude.getText().toString());
                data.put("longitude", inputLongitude.getText().toString());
                data.put("address", inputCrimeAddress.getText().toString());
                data.put("complainant", inputComplainantName.getText().toString());
                data.put("contact", inputComplainantContact.getText().toString());
                data.put("incident_summary", inputIncidentSummary.getText().toString());

                boolean inserted = dbHelper.insertCase(data);
                if (inserted) {
                    Toast.makeText(AddCaseActivity.this, "Case Added Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddCaseActivity.this, "Error Adding Case", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
