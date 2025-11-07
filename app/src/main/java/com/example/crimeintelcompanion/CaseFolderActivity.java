package com.example.crimeintelcompanion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CaseFolderActivity extends AppCompatActivity {

    Spinner caseSpinner;
    RecyclerView recyclerFolders;
    DatabaseHelper dbHelper;
    FolderAdapter folderAdapter;
    ArrayList<String> folderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_folder);

        dbHelper = new DatabaseHelper(this);
        caseSpinner = findViewById(R.id.spinnerCases);
        recyclerFolders = findViewById(R.id.recyclerFolders);

        recyclerFolders.setLayoutManager(new LinearLayoutManager(this));
        folderAdapter = new FolderAdapter(folderList);
        recyclerFolders.setAdapter(folderAdapter);

        loadCaseSpinner(); // ✅ Load cases and auto-select latest

        // === Spinner Change Listener ===
        caseSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedCase = (String) caseSpinner.getSelectedItem();
                if (selectedCase != null && !selectedCase.trim().isEmpty()) {
                    loadFoldersForCase(selectedCase);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // === Floating Button to Add Folder ===
        FloatingActionButton fabAddFolder = findViewById(R.id.fabAddFolder);
        fabAddFolder.setOnClickListener(v -> showAddFolderDialog());
    }

    /** Load cases and auto-select latest one **/
    private void loadCaseSpinner() {
        ArrayList<String> cases = dbHelper.getAllCases();

        if (cases.isEmpty()) {
            cases.add("No Cases Available");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cases);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        caseSpinner.setAdapter(adapter);

        // ✅ Auto-select latest case (last in list)
        if (!cases.isEmpty() && !cases.get(0).equals("No Cases Available")) {
            caseSpinner.setSelection(cases.size() - 1);
            // Load folders for the latest case immediately
            loadFoldersForCase(cases.get(cases.size() - 1));
        }
    }

    /** Show predefined folder options **/
    private void showAddFolderDialog() {
        String[] folderOptions = {"Kitchen", "Inside Building", "Outside", "Parking Area", "Roof", "Garden"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Folder Type");

        final Spinner folderSpinner = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, folderOptions);
        folderSpinner.setAdapter(spinnerAdapter);
        builder.setView(folderSpinner);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String selectedFolder = folderSpinner.getSelectedItem().toString();
            createCaseFolder(selectedFolder);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /** Create folder and refresh list **/
    private void createCaseFolder(String folderName) {
        if (caseSpinner.getSelectedItem() == null || caseSpinner.getSelectedItem().toString().equals("No Cases Available")) {
            Toast.makeText(this, "Please select a case first", Toast.LENGTH_SHORT).show();
            return;
        }

        String caseName = caseSpinner.getSelectedItem().toString();
        File caseDir = new File(getFilesDir(), caseName);
        if (!caseDir.exists()) caseDir.mkdirs();

        File newFolder = new File(caseDir, folderName);
        if (!newFolder.exists()) {
            boolean created = newFolder.mkdir();
            if (created) {
                Toast.makeText(this, "Folder '" + folderName + "' created for case: " + caseName, Toast.LENGTH_SHORT).show();
                loadFoldersForCase(caseName); // ✅ Refresh folder list
            } else {
                Toast.makeText(this, "Failed to create folder!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder already exists!", Toast.LENGTH_SHORT).show();
        }
    }

    /** Load folders for selected case **/
    private void loadFoldersForCase(String caseName) {
        folderList.clear();
        File caseDir = new File(getFilesDir(), caseName);

        if (caseDir.exists() && caseDir.isDirectory()) {
            String[] folders = caseDir.list();
            if (folders != null && folders.length > 0) {
                folderList.addAll(Arrays.asList(folders));
                Collections.sort(folderList);
            } else {
                folderList.add("No folders created yet");
            }
        } else {
            folderList.add("No folders created yet");
        }

        folderAdapter.notifyDataSetChanged();
    }
}
