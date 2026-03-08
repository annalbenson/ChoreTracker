package com.annabenson.tidy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String[] FREQUENCIES = {"Daily", "Weekly", "Biweekly", "Monthly", "As needed"};

    private RecyclerView dailyRecycler;
    private RecyclerView otherRecycler;
    private View todaySection, upcomingSection, sectionDivider, emptyState;
    private TextView tvTodayCount;
    private ChipGroup chipGroup;

    private final ArrayList<Chore> dailyChores    = new ArrayList<>();
    private final ArrayList<Chore> allOtherChores = new ArrayList<>(); // unfiltered
    private final ArrayList<Chore> otherChores    = new ArrayList<>(); // filtered view

    private enum Filter { ALL, OVERDUE, SOON, AS_NEEDED }
    private Filter activeFilter = Filter.ALL;

    private DailyChoreAdapter dailyAdapter;
    private ChoreAdapter otherAdapter;
    private DatabaseHandler db;
    private List<Room> rooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(this);
        rooms = db.loadRooms();

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
        chipGroup       = findViewById(R.id.chipGroup);

        // Daily horizontal scroller
        dailyRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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

        // Filter chips
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipAll)      activeFilter = Filter.ALL;
            else if (id == R.id.chipOverdue)  activeFilter = Filter.OVERDUE;
            else if (id == R.id.chipSoon)     activeFilter = Filter.SOON;
            else if (id == R.id.chipAsNeeded) activeFilter = Filter.AS_NEEDED;
            applyFilter();
        });

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

        // Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            } else {
                scheduleNotification();
            }
        } else {
            scheduleNotification();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleNotification();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChores();
    }

    private void refreshChores() {
        List<Chore> all = db.loadChores();

        dailyChores.clear();
        allOtherChores.clear();

        for (Chore c : all) {
            if ("Daily".equalsIgnoreCase(c.getFrequency())) dailyChores.add(c);
            else allOtherChores.add(c);
        }

        // Sort daily: not-done first, then name
        Collections.sort(dailyChores, (a, b) -> {
            if (a.isDoneToday() != b.isDoneToday()) return a.isDoneToday() ? 1 : -1;
            return a.getName().compareTo(b.getName());
        });

        // Sort other: soonest due first (overdue floats to top), as-needed last
        Collections.sort(allOtherChores, (a, b) -> {
            long da = a.getNextDue(), db2 = b.getNextDue();
            if (da == -1 && db2 == -1) return a.getName().compareTo(b.getName());
            if (da == -1) return 1;
            if (db2 == -1) return -1;
            if (da != db2) return Long.compare(da, db2);
            return a.getName().compareTo(b.getName());
        });

        dailyAdapter.notifyDataSetChanged();
        applyFilter();
    }

    private void applyFilter() {
        long today    = todaySeconds();
        long nextWeek = today + 7 * 86400L;

        otherChores.clear();
        for (Chore c : allOtherChores) {
            switch (activeFilter) {
                case OVERDUE:
                    if (c.isOverdue()) otherChores.add(c);
                    break;
                case SOON:
                    if (!c.isOverdue() && c.getNextDue() != -1 && c.getNextDue() < nextWeek)
                        otherChores.add(c);
                    break;
                case AS_NEEDED:
                    if (c.getNextDue() == -1) otherChores.add(c);
                    break;
                default:
                    otherChores.add(c);
                    break;
            }
        }
        otherAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        boolean hasDaily   = !dailyChores.isEmpty();
        boolean hasOther   = !otherChores.isEmpty();
        // Show empty state only when there are truly no chores at all
        boolean hasAnything = hasDaily || !allOtherChores.isEmpty();

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
            db.markDone(chore.getId(), chore.getFrequency());
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

    // ── Notifications ─────────────────────────────────────────────────────────

    private void scheduleNotification() {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= now) cal.add(Calendar.DAY_OF_YEAR, 1);
        long initialDelay = cal.getTimeInMillis() - now;

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "tidy_daily_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request);
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
                    db.markDone(chore.getId(), chore.getFrequency());
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
        Spinner roomSpinner = buildRoomSpinner(chore.getRoomId());

        LinearLayout container = buildDialogContainer(dp, nameInput, "Frequency", freqSpinner);
        addSpinnerRow(container, dp, "Room", roomSpinner);

        new AlertDialog.Builder(this)
                .setTitle("Edit Chore")
                .setView(container)
                .setPositiveButton("Save", (d, i) -> {
                    String name = nameInput.getText().toString().trim();
                    String freq = (String) freqSpinner.getSelectedItem();
                    int roomId  = roomIdFromSpinner(roomSpinner);
                    if (!name.isEmpty()) {
                        boolean saved = db.updateChore(chore.getId(), name, freq, roomId);
                        if (saved) refreshChores();
                        else Snackbar.make(otherRecycler, "A chore with that name already exists",
                                Snackbar.LENGTH_SHORT).show();
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
        Spinner roomSpinner = buildRoomSpinner(0);

        LinearLayout container = buildDialogContainer(dp, nameInput, "Frequency", freqSpinner);
        addSpinnerRow(container, dp, "Room", roomSpinner);

        new AlertDialog.Builder(this)
                .setTitle("Add Chore")
                .setView(container)
                .setPositiveButton("Add", (d, i) -> {
                    String name = nameInput.getText().toString().trim();
                    String freq = (String) freqSpinner.getSelectedItem();
                    int roomId  = roomIdFromSpinner(roomSpinner);
                    if (name.isEmpty()) return;
                    db.addChore(name, freq, roomId);
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

    private Spinner buildRoomSpinner(int selectedRoomId) {
        Spinner spinner = new Spinner(this);
        List<String> labels = new ArrayList<>();
        labels.add("Any room");
        for (Room r : rooms) labels.add(r.toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, labels);
        spinner.setAdapter(adapter);
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).id == selectedRoomId) { spinner.setSelection(i + 1); break; }
        }
        return spinner;
    }

    private int roomIdFromSpinner(Spinner roomSpinner) {
        int pos = roomSpinner.getSelectedItemPosition();
        return (pos == 0 || pos > rooms.size()) ? 0 : rooms.get(pos - 1).id;
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

    private void addSpinnerRow(LinearLayout container, float dp, String label, Spinner spinner) {
        int gap = (int) (12 * dp);
        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextColor(Color.parseColor("#6B7F77"));
        lbl.setTextSize(12f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = gap; lp.bottomMargin = (int) (4 * dp);
        lbl.setLayoutParams(lp);
        container.addView(lbl);
        container.addView(spinner);
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

    private long todaySeconds() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis() / 1000;
    }
}
