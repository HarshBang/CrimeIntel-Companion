package com.example.crimeintelcompanion;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

    CardView checkListCard, cameraCard, chatbotcard, fexpertCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        checkListCard = view.findViewById(R.id.checkListCard);
        cameraCard = view.findViewById(R.id.cameraCard);
        chatbotcard = view.findViewById(R.id.chatbotCard);
        fexpertCard = view.findViewById(R.id.fexpertCard);

        checkListCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CheckListActivity.class);
                startActivity(i);
            }
        });

        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), GeoCameraActivity.class);
                startActivity(i);
            }
        });

        chatbotcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ChatBotActivity.class);
                startActivity(i);
            }
        });

        fexpertCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CallExpertActivity.class);
                startActivity(i);
            }
        });



        return view;
    }

}