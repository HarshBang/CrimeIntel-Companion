package com.example.crimeintelcompanion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddCaseActivity extends AppCompatActivity {

    EditText inputFirNumber, inputCaseTitle, inputPoliceStation, inputLatitude, inputLongitude,
            inputCrimeAddress, inputComplainantName, inputComplainantContact, inputIncidentSummary, inputIncidentDatetime;
    Spinner spinnerCaseType;
    Button btnCreateCase;
    DatabaseHelper dbHelper;
    FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_case);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
        inputIncidentDatetime = findViewById(R.id.inputIncidentDatetime);
        btnCreateCase = findViewById(R.id.btnCreateCase);

        // --- Set current date as default ---
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        inputIncidentDatetime.setText(sdf.format(new Date()));

        // --- Auto fetch location on launch ---
        fetchCurrentLocation();

        // --- Date Picker on Incident Date field ---
        inputIncidentDatetime.setOnClickListener(v -> showDatePicker());

        // --- Save case button ---
        btnCreateCase.setOnClickListener(v -> saveCase());
    }

    /** Fetch current location and address automatically **/
    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    updateLocationFields(location);
                }
            });
        }
    }

    private void updateLocationFields(Location location) {
        inputLatitude.setText(String.valueOf(location.getLatitude()));
        inputLongitude.setText(String.valueOf(location.getLongitude()));

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                inputCrimeAddress.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Date Picker Dialog **/
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    inputIncidentDatetime.setText(sdf.format(calendar.getTime()));
                }, year, month, day);
        datePicker.show();
    }

    /** Save case to SQLite **/
    private void saveCase() {
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
        Toast.makeText(this,
                inserted ? "Case Added Successfully" : "Error Adding Case",
                Toast.LENGTH_SHORT).show();

        if (inserted) finish();
    }
}
