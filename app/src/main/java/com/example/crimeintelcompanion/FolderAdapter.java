package com.example.crimeintelcompanion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    ArrayList<String> folders;

    public FolderAdapter(ArrayList<String> folders) {
        this.folders = folders;
    }

    @NonNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.ViewHolder holder, int position) {
        String folderName = folders.get(position);
        holder.folderName.setText(folderName);

        holder.cardFolder.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GeoCameraActivity.class);
            intent.putExtra("folder_name", folderName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        CardView cardFolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            cardFolder = itemView.findViewById(R.id.cardFolder);
        }
    }
}
