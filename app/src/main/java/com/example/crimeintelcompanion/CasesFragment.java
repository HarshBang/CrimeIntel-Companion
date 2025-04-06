package com.example.crimeintelcompanion;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CasesFragment extends Fragment {

    FloatingActionButton newCase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cases, container, false);

        newCase = view.findViewById(R.id.newCase);

        view.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottomInset = insets.getSystemWindowInsetBottom();
            newCase.setTranslationY(-bottomInset);  // Adjust FAB position
            return insets;
        });

        newCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Add New Case.. \nComming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}