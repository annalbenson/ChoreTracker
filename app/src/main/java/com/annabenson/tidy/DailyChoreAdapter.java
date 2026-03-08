package com.annabenson.tidy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailyChoreAdapter extends RecyclerView.Adapter<DailyChoreAdapter.ViewHolder> {

    interface Listener {
        void onTap(Chore chore);
        void onLongPress(Chore chore);
    }

    private final List<Chore> chores;
    private final Listener listener;

    public DailyChoreAdapter(List<Chore> chores, Listener listener) {
        this.chores = chores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_chore_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chore chore = chores.get(position);
        holder.emoji.setText(emojiFor(chore.getName()));
        holder.name.setText(chore.getName());

        boolean done = chore.isDoneToday();
        holder.doneLabel.setVisibility(done ? View.VISIBLE : View.GONE);
        holder.itemView.setAlpha(done ? 0.6f : 1f);

        holder.itemView.setOnClickListener(v -> listener.onTap(chore));
        holder.itemView.setOnLongClickListener(v -> { listener.onLongPress(chore); return true; });
    }

    @Override
    public int getItemCount() { return chores.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView emoji, name, doneLabel;
        ViewHolder(View v) {
            super(v);
            emoji     = v.findViewById(R.id.tvChoreEmoji);
            name      = v.findViewById(R.id.tvChoreName);
            doneLabel = v.findViewById(R.id.tvDone);
        }
    }

    static String emojiFor(String choreName) {
        String s = choreName.toLowerCase();
        if (s.contains("dish") || s.contains("plates"))             return "🍽️";
        if (s.contains("oven"))                                      return "🫕";
        if (s.contains("stovetop") || s.contains("stove"))          return "🔥";
        if (s.contains("kitchen") || s.contains("counter"))         return "🍳";
        if (s.contains("fridge") || s.contains("refrigerator"))     return "🧊";
        if (s.contains("toilet") || s.contains("bathroom") ||
            s.contains("bath") || s.contains("shower"))             return "🛁";
        if (s.contains("sink"))                                      return "🚿";
        if (s.contains("mirror"))                                    return "🪞";
        if (s.contains("vacuum"))                                    return "🌀";
        if (s.contains("sweep") || s.contains("broom"))             return "🧹";
        if (s.contains("mop") || s.contains("floor"))               return "🪣";
        if (s.contains("laundry") || s.contains("clothes") ||
            s.contains("washing"))                                   return "👕";
        if (s.contains("sheet") || s.contains("bed") ||
            s.contains("pillow"))                                    return "🛏️";
        if (s.contains("dust"))                                      return "✨";
        if (s.contains("trash") || s.contains("garbage") ||
            s.contains("bin") || s.contains("recycling"))           return "🗑️";
        if (s.contains("window") || s.contains("glass"))            return "🪟";
        if (s.contains("plant") || s.contains("garden"))            return "🪴";
        if (s.contains("pet") || s.contains("dog") ||
            s.contains("cat") || s.contains("litter"))              return "🐾";
        if (s.contains("wipe") || s.contains("surface") ||
            s.contains("doorknob") || s.contains("switch"))         return "🧽";
        if (s.contains("declutter") || s.contains("tidy"))          return "📦";
        return "✅";
    }
}
