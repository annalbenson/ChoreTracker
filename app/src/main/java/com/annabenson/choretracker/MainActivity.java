package com.annabenson.choretracker;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener, View.OnLongClickListener {

    // constants
    private static final String TAG = "MainActivity";

    // recycler view
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    // attributes
    private MainActivity mainActivity = this;
    private ArrayList<Chore> chores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // rv
        recyclerView = findViewById(R.id.recyclerID);
        recyclerViewAdapter = new RecyclerViewAdapter(chores,mainActivity);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

    }

    @Override
    public void onClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        Chore chore = chores.get(pos);

        Toast.makeText(view.getContext(),"Clicked on " + chore.getName(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        Chore chore = chores.get(pos);
        Toast.makeText(view.getContext(), "Long clicked on " + chore.getName(),Toast.LENGTH_SHORT).show();
        return false;
    }

    protected void addNewChore(String input){
        chores.add(new Chore(input));
        Collections.sort(chores);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addChoreID:
                addChoreDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addChoreDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        Log.d(TAG, "addChoreDialog: START");
        final EditText editText = new EditText(mainActivity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);

        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: positive button clicked");
                String input = editText.getText().toString();

                // check if duplicate chore
                for(int j = 0; j < chores.size(); j++ ){
                    String name = chores.get(j).getName();
                    if(name.equals(input)){
                        mainActivity.duplicateDialog(input);
                    }
                }
                mainActivity.addNewChore(input);
            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: negative button clicked");
            }
        });

        builder.setMessage("Enter Name of Chore");
        builder.setTitle("Add Chore");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void duplicateDialog(String input){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Chore " + input + " already exists");
        builder.setTitle("Duplicate");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
