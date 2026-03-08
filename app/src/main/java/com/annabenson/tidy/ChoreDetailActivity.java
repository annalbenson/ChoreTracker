package com.annabenson.tidy;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChoreDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CHORE_ID = "chore_id";

    private DatabaseHandler db;
    private int choreId;
    private TextView tvChoreName;
    private TextView tvFrequency;
    private TextView tvLastDone;
    private LinearLayout historyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chore_detail);

        db = new DatabaseHandler(this);
        choreId = getIntent().getIntExtra(EXTRA_CHORE_ID, -1);
        if (choreId == -1) { finish(); return; }

        tvChoreName      = findViewById(R.id.tvChoreName);
        tvFrequency      = findViewById(R.id.tvFrequency);
        tvLastDone       = findViewById(R.id.tvLastDone);
        historyContainer = findViewById(R.id.historyContainer);
        ImageButton backButton  = findViewById(R.id.backButton);
        MaterialButton btnMarkDone = findViewById(R.id.btnMarkDone);

        backButton.setOnClickListener(v -> finish());
        btnMarkDone.setOnClickListener(v -> markDone());

        populateView();
    }

    private void populateView() {
        Chore chore = db.loadChore(choreId);
        if (chore == null) { finish(); return; }

        tvChoreName.setText(chore.getName());
        tvFrequency.setText(chore.getFrequency());

        long lastDone = chore.getLastCompletedAt();
        tvLastDone.setText(lastDone == -1 ? "Never" : formatDate(lastDone));

        historyContainer.removeAllViews();
        List<Long> timestamps = chore.getCompletionTimestamps();
        if (timestamps.isEmpty()) {
            historyContainer.addView(makeHistoryRow("No completions yet"));
        } else {
            // Show most recent 20
            int limit = Math.min(timestamps.size(), 20);
            for (int i = 0; i < limit; i++) {
                historyContainer.addView(makeHistoryRow(formatDate(timestamps.get(i))));
            }
            if (timestamps.size() > 20) {
                historyContainer.addView(makeHistoryRow(
                        "+ " + (timestamps.size() - 20) + " more…"));
            }
        }
    }

    private void markDone() {
        db.markDone(choreId);
        populateView();
    }

    private TextView makeHistoryRow(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        tv.setGravity(Gravity.START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
        tv.setLayoutParams(params);
        return tv;
    }

    private String formatDate(long unixSeconds) {
        return new SimpleDateFormat("EEE, MMM d yyyy · h:mm a", Locale.getDefault())
                .format(new Date(unixSeconds * 1000));
    }
}
