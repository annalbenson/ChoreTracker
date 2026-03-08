package com.annabenson.tidy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 5;
    private static final String DB_NAME = "TidyDb";

    // ── RoomTable ─────────────────────────────────────────────────────────────
    private static final String TABLE_ROOMS    = "RoomTable";
    private static final String COL_ROOM_ID    = "RoomId";
    private static final String COL_ROOM_NAME  = "RoomName";
    private static final String COL_ROOM_EMOJI = "RoomEmoji";

    // ── ChoreTable ────────────────────────────────────────────────────────────
    private static final String TABLE_CHORES      = "ChoreTable";
    private static final String COL_ID            = "ChoreId";
    private static final String COL_NAME          = "ChoreName";
    private static final String COL_FREQUENCY     = "ChoreFrequency";
    private static final String COL_NEXT_DUE      = "NextDue";   // unix seconds, NULL = as needed
    private static final String COL_CHORE_ROOM_FK = "RoomId";    // FK → RoomTable.RoomId

    // ── CompletionTable ───────────────────────────────────────────────────────
    private static final String TABLE_COMPLETIONS = "CompletionTable";
    private static final String COL_COMPLETION_ID = "CompletionId";
    private static final String COL_CHORE_ID_FK   = "ChoreId";
    private static final String COL_COMPLETED_AT  = "CompletedAt";

    // ── HomeProfileTable ──────────────────────────────────────────────────────
    private static final String TABLE_PROFILE      = "HomeProfileTable";
    private static final String COL_PROFILE_ID     = "Id";
    private static final String COL_PROFILE_NAME   = "Name";
    private static final String COL_HOME_TYPE      = "HomeType";
    private static final String COL_BEDROOMS       = "Bedrooms";
    private static final String COL_BATHROOMS      = "Bathrooms";
    private static final String COL_LAUNDRY        = "LaundryType";
    private static final String COL_HOUSEHOLD      = "HouseholdMembers";
    private static final String COL_CLEANING_STYLE = "CleaningStyle";
    private static final String COL_PAIN_POINTS    = "PainPoints";

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ROOMS + " (" +
                COL_ROOM_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ROOM_NAME  + " TEXT NOT NULL UNIQUE, " +
                COL_ROOM_EMOJI + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_CHORES + " (" +
                COL_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME          + " TEXT NOT NULL UNIQUE, " +
                COL_FREQUENCY     + " TEXT NOT NULL, " +
                COL_NEXT_DUE      + " INTEGER, " +
                COL_CHORE_ROOM_FK + " INTEGER REFERENCES " + TABLE_ROOMS + "(" + COL_ROOM_ID + ") ON DELETE SET NULL)");

        db.execSQL("CREATE TABLE " + TABLE_COMPLETIONS + " (" +
                COL_COMPLETION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CHORE_ID_FK   + " INTEGER NOT NULL, " +
                COL_COMPLETED_AT  + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_CHORE_ID_FK + ") REFERENCES " +
                TABLE_CHORES + "(" + COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX idx_completion_chore ON " +
                TABLE_COMPLETIONS + "(" + COL_CHORE_ID_FK + ")");

        db.execSQL("CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_PROFILE_ID     + " INTEGER PRIMARY KEY, " +
                COL_PROFILE_NAME   + " TEXT NOT NULL, " +
                COL_HOME_TYPE      + " TEXT NOT NULL, " +
                COL_BEDROOMS       + " INTEGER NOT NULL DEFAULT 1, " +
                COL_BATHROOMS      + " INTEGER NOT NULL DEFAULT 1, " +
                COL_LAUNDRY        + " TEXT, " +
                COL_HOUSEHOLD      + " TEXT, " +
                COL_CLEANING_STYLE + " TEXT, " +
                COL_PAIN_POINTS    + " TEXT)");

        seedRooms(db);
    }

    private void seedRooms(SQLiteDatabase db) {
        String[][] rooms = {
            {"Kitchen",      "🍳"},
            {"Bathroom",     "🛁"},
            {"Bedroom",      "🛏️"},
            {"Living Room",  "🛋️"},
            {"Office",       "💼"},
            {"Entryway",     "🚪"},
            {"Laundry Room", "👔"},
            {"Garage",       "🚗"}
        };
        for (String[] r : rooms) {
            ContentValues cv = new ContentValues();
            cv.put(COL_ROOM_NAME, r[0]);
            cv.put(COL_ROOM_EMOJI, r[1]);
            db.insertWithOnConflict(TABLE_ROOMS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        onCreate(db);
    }

    // ── Rooms ─────────────────────────────────────────────────────────────────

    public List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_ROOMS,
                new String[]{COL_ROOM_ID, COL_ROOM_NAME, COL_ROOM_EMOJI},
                null, null, null, null, COL_ROOM_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                rooms.add(new Room(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            }
            cursor.close();
        }
        return rooms;
    }

    // ── Chores ────────────────────────────────────────────────────────────────

    public void addChore(String name, String frequency) {
        addChore(name, frequency, 0);
    }

    public void addChore(String name, String frequency, int roomId) {
        long nextDue = calculateNextDue(frequency, startOfTodaySeconds());
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_FREQUENCY, frequency);
        if (nextDue == -1) values.putNull(COL_NEXT_DUE); else values.put(COL_NEXT_DUE, nextDue);
        if (roomId > 0) values.put(COL_CHORE_ROOM_FK, roomId); else values.putNull(COL_CHORE_ROOM_FK);
        getWritableDatabase().insertWithOnConflict(
                TABLE_CHORES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Returns false if the new name conflicts with an existing chore. */
    public boolean updateChore(int choreId, String name, String frequency, int roomId) {
        ArrayList<Long> completions = loadCompletions(choreId);
        long base = completions.isEmpty() ? startOfTodaySeconds() : completions.get(0);
        long nextDue = calculateNextDue(frequency, base);

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_FREQUENCY, frequency);
        if (nextDue == -1) values.putNull(COL_NEXT_DUE); else values.put(COL_NEXT_DUE, nextDue);
        if (roomId > 0) values.put(COL_CHORE_ROOM_FK, roomId); else values.putNull(COL_CHORE_ROOM_FK);

        long rows = getWritableDatabase().updateWithOnConflict(
                TABLE_CHORES, values, COL_ID + "=?",
                new String[]{String.valueOf(choreId)},
                SQLiteDatabase.CONFLICT_IGNORE);
        return rows > 0;
    }

    public void deleteChore(int choreId) {
        getWritableDatabase().delete(TABLE_CHORES, COL_ID + "=?",
                new String[]{String.valueOf(choreId)});
    }

    public void markDone(int choreId, String frequency) {
        long now = System.currentTimeMillis() / 1000;
        long nextDue = calculateNextDue(frequency, now);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues completion = new ContentValues();
            completion.put(COL_CHORE_ID_FK, choreId);
            completion.put(COL_COMPLETED_AT, now);
            db.insert(TABLE_COMPLETIONS, null, completion);

            ContentValues chore = new ContentValues();
            if (nextDue == -1) chore.putNull(COL_NEXT_DUE);
            else chore.put(COL_NEXT_DUE, nextDue);
            db.update(TABLE_CHORES, chore, COL_ID + "=?",
                    new String[]{String.valueOf(choreId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ── Scheduling ────────────────────────────────────────────────────────────

    private long calculateNextDue(String frequency, long fromSeconds) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(fromSeconds * 1000);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        switch (frequency.toLowerCase()) {
            case "daily":    cal.add(java.util.Calendar.DAY_OF_YEAR, 1);  break;
            case "weekly":   cal.add(java.util.Calendar.DAY_OF_YEAR, 7);  break;
            case "biweekly": cal.add(java.util.Calendar.DAY_OF_YEAR, 14); break;
            case "monthly":  cal.add(java.util.Calendar.MONTH, 1);        break;
            default:         return -1;
        }
        return cal.getTimeInMillis() / 1000;
    }

    private long startOfTodaySeconds() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis() / 1000;
    }

    public ArrayList<Chore> loadChores() {
        ArrayList<Chore> chores = new ArrayList<>();
        String sql = "SELECT c." + COL_ID + ", c." + COL_NAME + ", c." + COL_FREQUENCY +
                ", c." + COL_NEXT_DUE + ", r." + COL_ROOM_ID + ", r." + COL_ROOM_NAME +
                ", r." + COL_ROOM_EMOJI +
                " FROM " + TABLE_CHORES + " c" +
                " LEFT JOIN " + TABLE_ROOMS + " r ON c." + COL_CHORE_ROOM_FK + " = r." + COL_ROOM_ID +
                " ORDER BY c." + COL_NAME + " ASC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                long nextDue = cursor.isNull(3) ? -1 : cursor.getLong(3);
                Chore chore = new Chore(id, cursor.getString(1), cursor.getString(2),
                        nextDue, loadCompletions(id));
                if (!cursor.isNull(4)) {
                    chore.setRoom(cursor.getInt(4), cursor.getString(5), cursor.getString(6));
                }
                chores.add(chore);
            }
            cursor.close();
        }
        return chores;
    }

    public Chore loadChore(int choreId) {
        String sql = "SELECT c." + COL_ID + ", c." + COL_NAME + ", c." + COL_FREQUENCY +
                ", c." + COL_NEXT_DUE + ", r." + COL_ROOM_ID + ", r." + COL_ROOM_NAME +
                ", r." + COL_ROOM_EMOJI +
                " FROM " + TABLE_CHORES + " c" +
                " LEFT JOIN " + TABLE_ROOMS + " r ON c." + COL_CHORE_ROOM_FK + " = r." + COL_ROOM_ID +
                " WHERE c." + COL_ID + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(choreId)});
        if (cursor != null && cursor.moveToFirst()) {
            long nextDue = cursor.isNull(3) ? -1 : cursor.getLong(3);
            Chore chore = new Chore(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2), nextDue, loadCompletions(choreId));
            if (!cursor.isNull(4)) {
                chore.setRoom(cursor.getInt(4), cursor.getString(5), cursor.getString(6));
            }
            cursor.close();
            return chore;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    private ArrayList<Long> loadCompletions(int choreId) {
        ArrayList<Long> timestamps = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_COMPLETIONS,
                new String[]{COL_COMPLETED_AT},
                COL_CHORE_ID_FK + "=?", new String[]{String.valueOf(choreId)},
                null, null, COL_COMPLETED_AT + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) timestamps.add(cursor.getLong(0));
            cursor.close();
        }
        return timestamps;
    }

    // ── Home Profile ──────────────────────────────────────────────────────────

    public void saveProfile(HomeProfile profile) {
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_ID, 1);
        values.put(COL_PROFILE_NAME, profile.name);
        values.put(COL_HOME_TYPE, profile.homeType);
        values.put(COL_BEDROOMS, profile.bedrooms);
        values.put(COL_BATHROOMS, profile.bathrooms);
        values.put(COL_LAUNDRY, profile.laundryType);
        values.put(COL_HOUSEHOLD, profile.householdMembers);
        values.put(COL_CLEANING_STYLE, profile.cleaningStyle);
        values.put(COL_PAIN_POINTS, profile.painPoints);
        getWritableDatabase().insertWithOnConflict(
                TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public HomeProfile loadProfile() {
        Cursor cursor = getReadableDatabase().query(
                TABLE_PROFILE, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            HomeProfile p = new HomeProfile();
            p.name             = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_NAME));
            p.homeType         = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOME_TYPE));
            p.bedrooms         = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BEDROOMS));
            p.bathrooms        = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BATHROOMS));
            p.laundryType      = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAUNDRY));
            p.householdMembers = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOUSEHOLD));
            p.cleaningStyle    = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLEANING_STYLE));
            p.painPoints       = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAIN_POINTS));
            cursor.close();
            return p;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean hasProfile() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PROFILE, null);
        boolean exists = cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    public void resetAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_COMPLETIONS, null, null);
        db.delete(TABLE_CHORES, null, null);
        db.delete(TABLE_PROFILE, null, null);
        // Rooms are seeded data — not cleared on reset
    }
}
