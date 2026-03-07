package com.annabenson.tidy;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements ChoreAdapter.Listener {

    private RecyclerView recyclerView;
    private ChoreAdapter choreAdapter;
    private final ArrayList<Chore> chores = new ArrayList<>();
    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHandler = new DatabaseHandler(this);

        if (!databaseHandler.hasProfile()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerID);
        choreAdapter = new ChoreAdapter(chores, this);
        recyclerView.setAdapter(choreAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        findViewById(R.id.tillyFab).setOnClickListener(v ->
                startActivity(new Intent(this, TillyActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChores();
    }

    private void refreshChores() {
        chores.clear();
        chores.addAll(databaseHandler.loadChores());
        Collections.sort(chores);
        choreAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChoreClick(Chore chore, int position) {
        // Mark done on tap
        databaseHandler.markDone(chore.getId());
        Toast.makeText(this, chore.getName() + " marked done!", Toast.LENGTH_SHORT).show();
        refreshChores();
    }

    @Override
    public boolean onChoreLongClick(Chore chore, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete \"" + chore.getName() + "\"?")
                .setMessage("This will remove the chore and all its history.")
                .setPositiveButton("Delete", (d, i) -> {
                    databaseHandler.deleteChore(chore.getId());
                    refreshChores();
                })
                .setNegativeButton("Cancel", null)
                .show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addChoreID) {
            showAddChoreDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddChoreDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setHint("e.g. Clean kitchen");

        new AlertDialog.Builder(this)
                .setTitle("Add Chore")
                .setMessage("Enter chore name")
                .setView(input)
                .setPositiveButton("Add", (d, i) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    for (Chore c : chores) {
                        if (c.getName().equalsIgnoreCase(name)) {
                            Toast.makeText(this, "\"" + name + "\" already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    databaseHandler.addChore(name, "As needed");
                    refreshChores();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
