package com.annabenson.tidy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "TidyDb";

    private static final String TABLE_CHORES = "ChoreTable";
    private static final String COL_ID = "ChoreId";
    private static final String COL_NAME = "ChoreName";
    private static final String COL_FREQUENCY = "ChoreFrequency";

    private static final String TABLE_COMPLETIONS = "CompletionTable";
    private static final String COL_COMPLETION_ID = "CompletionId";
    private static final String COL_CHORE_ID_FK = "ChoreId";
    private static final String COL_COMPLETED_AT = "CompletedAt"; // unix seconds

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CHORES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL UNIQUE, " +
                COL_FREQUENCY + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_COMPLETIONS + " (" +
                COL_COMPLETION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CHORE_ID_FK + " INTEGER NOT NULL, " +
                COL_COMPLETED_AT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_CHORE_ID_FK + ") REFERENCES " + TABLE_CHORES + "(" + COL_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHORES);
        onCreate(db);
    }

    public void addChore(String name, String frequency) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_FREQUENCY, frequency);
        getWritableDatabase().insertWithOnConflict(TABLE_CHORES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void deleteChore(int choreId) {
        getWritableDatabase().delete(TABLE_CHORES, COL_ID + "=?",
                new String[]{String.valueOf(choreId)});
    }

    public void markDone(int choreId) {
        ContentValues values = new ContentValues();
        values.put(COL_CHORE_ID_FK, choreId);
        values.put(COL_COMPLETED_AT, System.currentTimeMillis() / 1000);
        getWritableDatabase().insert(TABLE_COMPLETIONS, null, values);
    }

    public ArrayList<Chore> loadChores() {
        ArrayList<Chore> chores = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_CHORES,
                new String[]{COL_ID, COL_NAME, COL_FREQUENCY},
                null, null, null, null, COL_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String frequency = cursor.getString(2);
                chores.add(new Chore(id, name, frequency, loadCompletions(id)));
            }
            cursor.close();
        }
        return chores;
    }

    private ArrayList<Long> loadCompletions(int choreId) {
        ArrayList<Long> timestamps = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_COMPLETIONS,
                new String[]{COL_COMPLETED_AT},
                COL_CHORE_ID_FK + "=?",
                new String[]{String.valueOf(choreId)},
                null, null, COL_COMPLETED_AT + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                timestamps.add(cursor.getLong(0));
            }
            cursor.close();
        }
        return timestamps;
    }
}
