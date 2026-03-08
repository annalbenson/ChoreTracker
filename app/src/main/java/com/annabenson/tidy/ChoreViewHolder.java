package com.annabenson.tidy;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class ChoreViewHolder extends RecyclerView.ViewHolder {

    private final TextView emojiText;
    private final TextView nameText;
    private final TextView frequencyText;
    private final TextView lastDoneText;

    public ChoreViewHolder(View view) {
        super(view);
        emojiText     = view.findViewById(R.id.emojiID);
        nameText      = view.findViewById(R.id.nameID);
        frequencyText = view.findViewById(R.id.frequencyID);
        lastDoneText  = view.findViewById(R.id.lastDoneID);
    }

    public void bind(Chore chore) {
        emojiText.setText(DailyChoreAdapter.emojiFor(chore.getName()));
        nameText.setText(chore.getName());

        // Secondary line: "Weekly  ·  🍳 Kitchen" or just "Weekly"
        String secondary = chore.getFrequency();
        if (chore.hasRoom()) secondary += "  ·  " + chore.getRoomEmoji() + " " + chore.getRoomName();
        frequencyText.setText(secondary);

        lastDoneText.setText(chore.getDueLabel());
        lastDoneText.setTextColor(chore.isOverdue()
                ? Color.parseColor("#C07850")
                : Color.parseColor("#6B7F77"));
    }
}
