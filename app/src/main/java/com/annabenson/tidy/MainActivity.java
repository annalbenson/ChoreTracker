package com.annabenson.tidy;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] FREQUENCIES = {"Daily", "Weekly", "Biweekly", "Monthly", "As needed"};

    private RecyclerView dailyRecycler;
    private RecyclerView otherRecycler;
    private View todaySection, upcomingSection, sectionDivider, emptyState;
    private TextView tvTodayCount;

    private final ArrayList<Chore> dailyChores = new ArrayList<>();
    private final ArrayList<Chore> otherChores = new ArrayList<>();

    private DailyChoreAdapter dailyAdapter;
    private ChoreAdapter otherAdapter;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(this);

        if (!db.hasProfile()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        todaySection    = findViewById(R.id.todaySection);
        upcomingSection = findViewById(R.id.upcomingSection);
        sectionDivider  = findViewById(R.id.sectionDivider);
        emptyState      = findViewById(R.id.emptyState);
        tvTodayCount    = findViewById(R.id.tvTodayCount);
        dailyRecycler   = findViewById(R.id.dailyRecycler);
        otherRecycler   = findViewById(R.id.otherRecycler);

        // Daily horizontal scroller
        LinearLayoutManager hLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dailyRecycler.setLayoutManager(hLayout);
        dailyAdapter = new DailyChoreAdapter(dailyChores, new DailyChoreAdapter.Listener() {
            @Override public void onTap(Chore chore) { handleDailyTap(chore); }
            @Override public void onLongPress(Chore chore) { showEditDeleteDialog(chore); }
        });
        dailyRecycler.setAdapter(dailyAdapter);

        // Upcoming vertical list
        otherRecycler.setLayoutManager(new LinearLayoutManager(this));
        otherAdapter = new ChoreAdapter(otherChores, new ChoreAdapter.Listener() {
            @Override public void onChoreClick(Chore chore, int position) { openDetail(chore); }
            @Override public boolean onChoreLongClick(Chore chore, int position) {
                showEditDeleteDialog(chore); return true;
            }
        });
        otherRecycler.setAdapter(otherAdapter);
        attachSwipeHandler();

        // Greeting
        HomeProfile profile = db.loadProfile();
        String name = (profile != null && profile.name != null && !profile.name.isEmpty())
                ? profile.name : null;
        ((TextView) findViewById(R.id.tvGreeting)).setText(buildGreeting(name));

        findViewById(R.id.tillyFab).setOnClickListener(v ->
                startActivity(new Intent(this, TillyActivity.class)));
        findViewById(R.id.addFab).setOnClickListener(v -> showAddChoreDialog());
        findViewById(R.id.btnDeclutter).setOnClickListener(v ->
                startActivity(new Intent(this, DeclutterActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChores();
    }

    private void refreshChores() {
        List<Chore> all = db.loadChores();

        dailyChores.clear();
        otherChores.clear();

        for (Chore c : all) {
            if ("Daily".equalsIgnoreCase(c.getFrequency())) dailyChores.add(c);
            else otherChores.add(c);
        }

        // Sort daily: not-done first, then name
        Collections.sort(dailyChores, (a, b) -> {
            if (a.isDoneToday() != b.isDoneToday())
                return a.isDoneToday() ? 1 : -1;
            return a.getName().compareTo(b.getName());
        });

        // Sort other: by frequency priority, then name
        Collections.sort(otherChores, (a, b) -> {
            int pa = freqPriority(a.getFrequency()), pb = freqPriority(b.getFrequency());
            if (pa != pb) return pa - pb;
            return a.getName().compareTo(b.getName());
        });

        dailyAdapter.notifyDataSetChanged();
        otherAdapter.notifyDataSetChanged();

        updateVisibility();
    }

    private void updateVisibility() {
        boolean hasDaily   = !dailyChores.isEmpty();
        boolean hasOther   = !otherChores.isEmpty();
        boolean hasAnything = hasDaily || hasOther;

        todaySection.setVisibility(hasDaily ? View.VISIBLE : View.GONE);
        upcomingSection.setVisibility(hasOther ? View.VISIBLE : View.GONE);
        sectionDivider.setVisibility(hasDaily && hasOther ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasAnything ? View.GONE : View.VISIBLE);

        if (hasDaily) {
            long doneCount = dailyChores.stream().filter(Chore::isDoneToday).count();
            tvTodayCount.setText(doneCount + " of " + dailyChores.size() + " done");
        }
    }

    private void handleDailyTap(Chore chore) {
        if (!chore.isDoneToday()) {
            db.markDone(chore.getId());
            refreshChores();
            Snackbar.make(dailyRecycler, chore.getName() + " — done! ✓", Snackbar.LENGTH_SHORT).show();
        } else {
            openDetail(chore);
        }
    }

    private void openDetail(Chore chore) {
        Intent intent = new Intent(this, ChoreDetailActivity.class);
        intent.putExtra(ChoreDetailActivity.EXTRA_CHORE_ID, chore.getId());
        startActivity(intent);
    }

    // ── Swipe handler (upcoming list only) ───────────────────────────────────

    private void attachSwipeHandler() {
        Paint paint = new Paint();
        float dp = getResources().getDisplayMetrics().density;

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh,
                                  RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                Chore chore = otherChores.get(pos);
                if (direction == ItemTouchHelper.RIGHT) {
                    db.markDone(chore.getId());
                    refreshChores();
                    Snackbar.make(otherRecycler, chore.getName() + " — done! ✓",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    otherAdapter.notifyItemChanged(pos);
                    showEditDeleteDialog(chore);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView rv, RecyclerView.ViewHolder vh,
                                    float dX, float dY, int state, boolean active) {
                View item = vh.itemView;
                if (dX > 0) {
                    paint.setColor(Color.parseColor("#6B9E8A"));
                    c.drawRect(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom(), paint);
                    drawLabel(c, paint, "✓  Done", item, true, dX, dp);
                } else if (dX < 0) {
                    paint.setColor(Color.parseColor("#C07850"));
                    c.drawRect(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom(), paint);
                    drawLabel(c, paint, "Edit / Delete", item, false, dX, dp);
                }
                super.onChildDraw(c, rv, vh, dX, dY, state, active);
            }
        }).attachToRecyclerView(otherRecycler);
    }

    private void drawLabel(Canvas c, Paint paint, String label,
                           View item, boolean leftAlign, float dX, float dp) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(13 * dp);
        paint.setAntiAlias(true);
        float w = paint.measureText(label);
        float cy = item.getTop() + item.getHeight() / 2f + paint.getTextSize() / 3f;
        if (leftAlign && dX > w + 40 * dp)
            c.drawText(label, item.getLeft() + 20 * dp, cy, paint);
        else if (!leftAlign && -dX > w + 40 * dp)
            c.drawText(label, item.getRight() - w - 20 * dp, cy, paint);
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private void showEditDeleteDialog(Chore chore) {
        new AlertDialog.Builder(this)
                .setTitle(chore.getName())
                .setItems(new String[]{"Edit", "Delete"}, (d, which) -> {
                    if (which == 0) showEditDialog(chore);
                    else confirmDelete(chore);
                }).show();
    }

    private void showEditDialog(Chore chore) {
        float dp = getResources().getDisplayMetrics().density;
        EditText nameInput = new EditText(this);
        nameInput.setText(chore.getName());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setSelectAllOnFocus(true);

        Spinner freqSpinner = buildFreqSpinner(chore.getFrequency());

        LinearLayout container = buildDialogContainer(dp, nameInput, "Frequency", freqSpinner);

        new AlertDialog.Builder(this)
                .setTitle("Edit Chore")
                .setView(container)
                .setPositiveButton("Save", (d, i) -> {
                    String name = nameInput.getText().toString().trim();
                    String freq = (String) freqSpinner.getSelectedItem();
                    if (!name.isEmpty()) {
                        boolean saved = db.updateChore(chore.getId(), name, freq);
                        if (saved) refreshChores();
                        else Snackbar.make(otherRecycler, "A chore with that name already exists", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showAddChoreDialog() {
        float dp = getResources().getDisplayMetrics().density;
        EditText nameInput = new EditText(this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setHint("e.g. Wipe down kitchen");

        Spinner freqSpinner = buildFreqSpinner("Weekly");

        LinearLayout container = buildDialogContainer(dp, nameInput, "Frequency", freqSpinner);

        new AlertDialog.Builder(this)
                .setTitle("Add Chore")
                .setView(container)
                .setPositiveButton("Add", (d, i) -> {
                    String name = nameInput.getText().toString().trim();
                    String freq = (String) freqSpinner.getSelectedItem();
                    if (name.isEmpty()) return;
                    db.addChore(name, freq);
                    refreshChores();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void confirmDelete(Chore chore) {
        new AlertDialog.Builder(this)
                .setTitle("Delete \"" + chore.getName() + "\"?")
                .setMessage("This will remove the chore and all its history.")
                .setPositiveButton("Delete", (d, i) -> { db.deleteChore(chore.getId()); refreshChores(); })
                .setNegativeButton("Cancel", null).show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Spinner buildFreqSpinner(String selected) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, FREQUENCIES);
        spinner.setAdapter(adapter);
        for (int i = 0; i < FREQUENCIES.length; i++) {
            if (FREQUENCIES[i].equalsIgnoreCase(selected)) { spinner.setSelection(i); break; }
        }
        return spinner;
    }

    private LinearLayout buildDialogContainer(float dp, EditText nameInput,
                                               String label, Spinner spinner) {
        int pad = (int) (20 * dp);
        int gap = (int) (12 * dp);

        TextView freqLabel = new TextView(this);
        freqLabel.setText(label);
        freqLabel.setTextColor(Color.parseColor("#6B7F77"));
        freqLabel.setTextSize(12f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = gap; lp.bottomMargin = (int) (4 * dp);
        freqLabel.setLayoutParams(lp);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, gap, pad, 0);
        container.addView(nameInput);
        container.addView(freqLabel);
        container.addView(spinner);
        return container;
    }

    private String buildGreeting(String name) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String prefix, suffix;
        if (hour >= 5 && hour < 12)       { prefix = "Good morning";    suffix = "Let's start the day fresh 🌿"; }
        else if (hour >= 12 && hour < 17) { prefix = "Happy afternoon"; suffix = "Let's get cleaning 🌿"; }
        else if (hour >= 17 && hour < 21) { prefix = "Good evening";    suffix = "A little goes a long way 🌿"; }
        else                              { prefix = "Still up";        suffix = "Even small wins count 🌿"; }
        String namepart = (name != null) ? ", " + name + "!" : "!";
        return prefix + namepart + " " + suffix;
    }

    private int freqPriority(String freq) {
        switch (freq.toLowerCase()) {
            case "weekly":   return 0;
            case "biweekly": return 1;
            case "monthly":  return 2;
            default:         return 3;
        }
    }
}
