package com.annabenson.choretracker;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    public static final String TAG = "RecyclerViewAdapter";

    private ArrayList<Chore> chores;
    private MainActivity mainActivity;

    public RecyclerViewAdapter(ArrayList<Chore> chores, MainActivity ma){
        this.chores = chores;
        mainActivity = ma;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chore_list_row,parent,false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return  new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Chore chore = chores.get(position);
        holder.name.setText(chore.getName());
        //
    }

    @Override
    public int getItemCount() {
        return chores.size();
    }
}
