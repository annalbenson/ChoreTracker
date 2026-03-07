package com.annabenson.tidy;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChoreViewHolder extends RecyclerView.ViewHolder {

    private final TextView nameText;
    private final TextView frequencyText;
    private final TextView lastDoneText;

    public ChoreViewHolder(View view) {
        super(view);
        nameText = view.findViewById(R.id.nameID);
        frequencyText = view.findViewById(R.id.frequencyID);
        lastDoneText = view.findViewById(R.id.lastDoneID);
    }

    public void bind(Chore chore) {
        nameText.setText(chore.getName());
        frequencyText.setText(chore.getFrequency());

        long lastDone = chore.getLastCompletedAt();
        if (lastDone == -1) {
            lastDoneText.setText("Never done");
        } else {
            String date = new SimpleDateFormat("MMM d", Locale.getDefault())
                    .format(new Date(lastDone * 1000));
            lastDoneText.setText("Last done: " + date);
        }
    }
}
