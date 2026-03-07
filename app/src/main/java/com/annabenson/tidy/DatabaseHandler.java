package com.annabenson.tidy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "TidyDb";

    private static final String TABLE_CHORES = "ChoreTable";
    private static final String COL_ID = "ChoreId";
    private static final String COL_NAME = "ChoreName";
    private static final String COL_FREQUENCY = "ChoreFrequency";

    private static final String TABLE_COMPLETIONS = "CompletionTable";
    private static final String COL_COMPLETION_ID = "CompletionId";
    private static final String COL_CHORE_ID_FK = "ChoreId";
    private static final String COL_COMPLETED_AT = "CompletedAt";

    private static final String TABLE_PROFILE = "HomeProfileTable";
    private static final String COL_PROFILE_NAME = "Name";
    private static final String COL_HOME_TYPE = "HomeType";
    private static final String COL_BEDROOMS = "Bedrooms";
    private static final String COL_BATHROOMS = "Bathrooms";
    private static final String COL_LAUNDRY = "LaundryType";
    private static final String COL_HOUSEHOLD = "HouseholdMembers";
    private static final String COL_CLEANING_STYLE = "CleaningStyle";
    private static final String COL_PAIN_POINTS = "PainPoints";

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

        db.execSQL("CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_PROFILE_NAME + " TEXT, " +
                COL_HOME_TYPE + " TEXT, " +
                COL_BEDROOMS + " INTEGER DEFAULT 1, " +
                COL_BATHROOMS + " INTEGER DEFAULT 1, " +
                COL_LAUNDRY + " TEXT, " +
                COL_HOUSEHOLD + " TEXT, " +
                COL_CLEANING_STYLE + " TEXT, " +
                COL_PAIN_POINTS + " TEXT)");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        onCreate(db);
    }

    // ── Chores ────────────────────────────────────────────────────────────────

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

    // ── Home Profile ──────────────────────────────────────────────────────────

    public void saveProfile(HomeProfile profile) {
        getWritableDatabase().delete(TABLE_PROFILE, null, null); // single-row table
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_NAME, profile.name);
        values.put(COL_HOME_TYPE, profile.homeType);
        values.put(COL_BEDROOMS, profile.bedrooms);
        values.put(COL_BATHROOMS, profile.bathrooms);
        values.put(COL_LAUNDRY, profile.laundryType);
        values.put(COL_HOUSEHOLD, profile.householdMembers);
        values.put(COL_CLEANING_STYLE, profile.cleaningStyle);
        values.put(COL_PAIN_POINTS, profile.painPoints);
        getWritableDatabase().insert(TABLE_PROFILE, null, values);
    }

    public HomeProfile loadProfile() {
        Cursor cursor = getReadableDatabase().query(
                TABLE_PROFILE, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            HomeProfile p = new HomeProfile();
            p.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_NAME));
            p.homeType = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOME_TYPE));
            p.bedrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BEDROOMS));
            p.bathrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BATHROOMS));
            p.laundryType = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAUNDRY));
            p.householdMembers = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOUSEHOLD));
            p.cleaningStyle = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLEANING_STYLE));
            p.painPoints = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAIN_POINTS));
            cursor.close();
            return p;
        }
        return null;
    }

    public boolean hasProfile() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PROFILE, null);
        boolean exists = cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0;
        if (cursor != null) cursor.close();
        return exists;
    }
}
