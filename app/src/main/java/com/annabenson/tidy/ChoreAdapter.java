package com.annabenson.tidy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChoreAdapter extends RecyclerView.Adapter<ChoreViewHolder> {

    public interface Listener {
        void onChoreClick(Chore chore, int position);
        boolean onChoreLongClick(Chore chore, int position);
    }

    private final ArrayList<Chore> chores;
    private final Listener listener;

    public ChoreAdapter(ArrayList<Chore> chores, Listener listener) {
        this.chores = chores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chore_list_row, parent, false);
        return new ChoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoreViewHolder holder, int position) {
        Chore chore = chores.get(position);
        holder.bind(chore);
        holder.itemView.setOnClickListener(v -> listener.onChoreClick(chore, position));
        holder.itemView.setOnLongClickListener(v -> listener.onChoreLongClick(chore, position));
    }

    @Override
    public int getItemCount() {
        return chores.size();
    }
}
