package com.annabenson.choretracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Date;
import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    private static final int DATABASE_VERSION = 1; //
    private static final String DATABASE_NAME = "ChoreAppDB";
    private static final String CHORE_TABLE_NAME = "ChoreTable";
    private static final String DATES_TABLE_NAME = "DatesTable";

    // columns
    private static final String ID = "ChoreID";
    private static final String NAME = "ChoreName";
    private static final String FREQUENCY = "ChoreFrequency";


    // Create table to hold Chore Data
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + CHORE_TABLE_NAME + " (" +
                    ID + "INT not null unique," +
                    NAME + " TEXT not null unique," +
                    FREQUENCY + " TEXT not null),"
            ;

    // Create empty table to hold Dates Data
    private static final String SQL_CREATE_DATES_TABLE =
            "CREATE TABLE " + DATES_TABLE_NAME;

    // Database itself
    private SQLiteDatabase database;

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
        Log.d(TAG, "DatabaseHandler: Creator Done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //
    }

    public ArrayList<Chore> loadChores(){
        Log.d(TAG, "loadChores: START");
        ArrayList<Chore> chores = new ArrayList<>();

        Cursor cursor = database.query(
                CHORE_TABLE_NAME,
                new String [] {ID, NAME, FREQUENCY},
                null, null,
                null, null, null);

        if (cursor != null){
            cursor.moveToFirst(); // important
            for(int i = 0; i < cursor.getCount(); i++){
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String frequency = cursor.getString(2);
                Chore c = new Chore(id,name,frequency, null);
                chores.add(c);
                cursor.moveToNext();
            }
            cursor.close();
        }

        // load dates using the choreIds in the newly created Chore objects


        for(int i = 0; i < chores.size(); i++){
            // get chore id
            int id = chores.get(i).getId();
            String column = "Chore" + id;
            // query for column in DatesTable
            Cursor cursor1 = database.query(
                DATES_TABLE_NAME,
                    new String[] {column},
                    null,
                    null,
                    null,
                    null,
                    null);
            if (cursor1 != null){
                cursor1.moveToFirst(); // important
                for(int j = 0; j < cursor1.getCount(); j++){
                  //Date date = cursor1.getDate() <-- need to figure out how to store and then fetch date objects
                }
            }

        }



        return chores;
    }
}
