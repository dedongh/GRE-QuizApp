package com.example.gre.Adapter;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gre.R;

public class CategoryAdapter extends RecyclerView.ViewHolder {

    public TextView category_name, user_score;
    public ProgressBar current_progress;

    public CategoryAdapter(@NonNull View itemView) {
        super(itemView);

        category_name = itemView.findViewById(R.id.cat_name);
        user_score = itemView.findViewById(R.id.score);
        current_progress = itemView.findViewById(R.id.cat_progress);
    }
}
