package com.annabenson.tidy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface ChipListener {
        void onChipsSelected(List<String> selected);
    }

    private static final int VIEW_TILLY = 0;
    private static final int VIEW_USER = 1;
    private static final int VIEW_CHIPS = 2;

    private final List<OnboardingMessage> messages = new ArrayList<>();
    private ChipListener chipListener;

    public void setChipListener(ChipListener listener) {
        this.chipListener = listener;
    }

    public void addMessage(OnboardingMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void replaceMessage(int index, OnboardingMessage message) {
        if (index >= 0 && index < messages.size()) {
            messages.set(index, message);
            notifyItemChanged(index);
        }
    }

    public int getItemCount() {
        return messages.size();
    }

    /** Disables the most recent CHIPS row only, leaving future rows unaffected. */
    public void disableLastChips() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).type == OnboardingMessage.Type.CHIPS) {
                messages.get(i).chipsEnabled = false;
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).type) {
            case USER:  return VIEW_USER;
            case CHIPS: return VIEW_CHIPS;
            default:    return VIEW_TILLY;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_USER) {
            return new UserHolder(inflater.inflate(R.layout.bubble_user, parent, false));
        } else if (viewType == VIEW_CHIPS) {
            return new ChipsHolder(inflater.inflate(R.layout.bubble_chips, parent, false));
        } else {
            return new TillyHolder(inflater.inflate(R.layout.bubble_tilly, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OnboardingMessage msg = messages.get(position);
        if (holder instanceof TillyHolder) {
            ((TillyHolder) holder).text.setText(msg.text);
        } else if (holder instanceof UserHolder) {
            ((UserHolder) holder).text.setText(msg.text);
        } else if (holder instanceof ChipsHolder) {
            ChipsHolder ch = (ChipsHolder) holder;
            ch.chipGroup.removeAllViews();
            List<String> selected = new ArrayList<>();
            for (String label : msg.chips) {
                Chip chip = new Chip(ch.chipGroup.getContext());
                chip.setText(label);
                chip.setCheckable(true);
                chip.setEnabled(msg.chipsEnabled);
                chip.setOnCheckedChangeListener((v, checked) -> {
                    if (checked) selected.add(label);
                    else selected.remove(label);
                });
                ch.chipGroup.addView(chip);
            }
            // Done chip
            Chip done = new Chip(ch.chipGroup.getContext());
            done.setText("Done ✓");
            done.setEnabled(msg.chipsEnabled);
            done.setOnClickListener(v -> {
                if (chipListener != null) chipListener.onChipsSelected(new ArrayList<>(selected));
            });
            ch.chipGroup.addView(done);
        }
    }

    static class TillyHolder extends RecyclerView.ViewHolder {
        TextView text;
        TillyHolder(View v) { super(v); text = v.findViewById(R.id.tillyText); }
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        TextView text;
        UserHolder(View v) { super(v); text = v.findViewById(R.id.userText); }
    }

    static class ChipsHolder extends RecyclerView.ViewHolder {
        ChipGroup chipGroup;
        ChipsHolder(View v) { super(v); chipGroup = v.findViewById(R.id.chipGroup); }
    }
}
