package com.annabenson.choretracker;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder{

    public static final String TAG = "RecyclerViewHolder";

    public TextView name;

    public RecyclerViewHolder(View view){
        super(view);
        name = view.findViewById(R.id.nameID);
    }
}
