package com.example.crimeintelcompanion;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    CardView checkListCard, cameraCard, chatbotcard, fexpertCard;
    Spinner caseSpinner;
    DatabaseHelper dbHelper;
    ArrayList<String> caseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        checkListCard = view.findViewById(R.id.addCaseCard);
        cameraCard = view.findViewById(R.id.cameraCard);
        chatbotcard = view.findViewById(R.id.chatbotCard);
        fexpertCard = view.findViewById(R.id.fexpertCard);
        caseSpinner = view.findViewById(R.id.caseSpinner);

        dbHelper = new DatabaseHelper(getContext());
        loadCaseSpinner();  // ✅ load cases dynamically

        // ===== Card Clicks =====
        checkListCard.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AddCaseActivity.class);
            startActivity(i);
        });

        cameraCard.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), GeoCameraActivity.class);
            startActivity(i);
        });

        chatbotcard.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), ChatBotActivity.class);
            startActivity(i);
        });

        fexpertCard.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), CallExpertActivity.class);
            startActivity(i);
        });

        // ===== Spinner Selection =====
        caseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0 && !caseList.get(i).equals("No Cases Available")) {
                    Toast.makeText(getContext(), "Selected: " + caseList.get(i), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return view;
    }

    private void loadCaseSpinner() {
        caseList = new ArrayList<>();
        caseList.add("Select Case");

        // Fetch case titles from database
        ArrayList<String> titles = dbHelper.getAllCases();
        if (titles.size() == 0) {
            caseList.add("No Cases Available");
        } else {
            caseList.addAll(titles);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, caseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        caseSpinner.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCaseSpinner(); // ✅ Refresh cases whenever you return from AddCaseActivity
    }
}
