package com.annabenson.tidy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 6;
    private static final String DB_NAME = "TidyDb";

    // ── UserAccountsTable ─────────────────────────────────────────────────────
    private static final String TABLE_USERS     = "UserAccountsTable";
    private static final String COL_USER_ID     = "Id";
    private static final String COL_USER_EMAIL  = "Email";
    private static final String COL_USER_PASS   = "Password";
    private static final String COL_USER_NAME   = "Name";

    // ── RoomTable ─────────────────────────────────────────────────────────────
    private static final String TABLE_ROOMS    = "RoomTable";
    private static final String COL_ROOM_ID    = "RoomId";
    private static final String COL_ROOM_NAME  = "RoomName";
    private static final String COL_ROOM_EMOJI = "RoomEmoji";

    // ── ChoreTable ────────────────────────────────────────────────────────────
    private static final String TABLE_CHORES      = "ChoreTable";
    private static final String COL_ID            = "ChoreId";
    private static final String COL_CHORE_USER_FK = "UserId";
    private static final String COL_NAME          = "ChoreName";
    private static final String COL_FREQUENCY     = "ChoreFrequency";
    private static final String COL_NEXT_DUE      = "NextDue";
    private static final String COL_CHORE_ROOM_FK = "RoomId";

    // ── CompletionTable ───────────────────────────────────────────────────────
    private static final String TABLE_COMPLETIONS = "CompletionTable";
    private static final String COL_COMPLETION_ID = "CompletionId";
    private static final String COL_CHORE_ID_FK   = "ChoreId";
    private static final String COL_COMPLETED_AT  = "CompletedAt";

    // ── HomeProfileTable ──────────────────────────────────────────────────────
    // UserId is the PK here — one profile per user, CONFLICT_REPLACE acts as upsert
    private static final String TABLE_PROFILE      = "HomeProfileTable";
    private static final String COL_PROFILE_USER   = "UserId";
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
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COL_USER_PASS  + " TEXT NOT NULL, " +
                COL_USER_NAME  + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_ROOMS + " (" +
                COL_ROOM_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ROOM_NAME  + " TEXT NOT NULL UNIQUE, " +
                COL_ROOM_EMOJI + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_CHORES + " (" +
                COL_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CHORE_USER_FK + " INTEGER NOT NULL REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                COL_NAME          + " TEXT NOT NULL, " +
                COL_FREQUENCY     + " TEXT NOT NULL, " +
                COL_NEXT_DUE      + " INTEGER, " +
                COL_CHORE_ROOM_FK + " INTEGER REFERENCES " + TABLE_ROOMS + "(" + COL_ROOM_ID + ") ON DELETE SET NULL, " +
                "UNIQUE(" + COL_CHORE_USER_FK + ", " + COL_NAME + "))");

        db.execSQL("CREATE TABLE " + TABLE_COMPLETIONS + " (" +
                COL_COMPLETION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CHORE_ID_FK   + " INTEGER NOT NULL, " +
                COL_COMPLETED_AT  + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_CHORE_ID_FK + ") REFERENCES " +
                TABLE_CHORES + "(" + COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX idx_completion_chore ON " +
                TABLE_COMPLETIONS + "(" + COL_CHORE_ID_FK + ")");

        // UserId is the PK — one profile row per user, replaced via CONFLICT_REPLACE
        db.execSQL("CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_PROFILE_USER   + " INTEGER PRIMARY KEY REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new user account. Returns the new userId, or -1 if the email is already taken.
     * Password is stored as SHA-256 hash — ready for backend migration.
     */
    public int createUser(String email, String password, String name) {
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, email.trim().toLowerCase());
        cv.put(COL_USER_PASS, hashPassword(password));
        cv.put(COL_USER_NAME, name.trim());
        long id = getWritableDatabase().insertWithOnConflict(
                TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return (int) id;
    }

    /**
     * Verifies credentials. Returns userId on success, or -1 if email/password don't match.
     */
    public int loginUser(String email, String password) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=? AND " + COL_USER_PASS + "=?",
                new String[]{email.trim().toLowerCase(), hashPassword(password)},
                null, null, null);
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) userId = cursor.getInt(0);
        if (cursor != null) cursor.close();
        return userId;
    }

    public String getUserName(int userId) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_USERS, new String[]{COL_USER_NAME},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        String name = null;
        if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
        if (cursor != null) cursor.close();
        return name;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // SHA-256 is always available on Android
        }
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

    public void addChore(String name, String frequency, int roomId, int userId) {
        long nextDue = calculateNextDue(frequency, startOfTodaySeconds());
        ContentValues values = new ContentValues();
        values.put(COL_CHORE_USER_FK, userId);
        values.put(COL_NAME, name);
        values.put(COL_FREQUENCY, frequency);
        if (nextDue == -1) values.putNull(COL_NEXT_DUE); else values.put(COL_NEXT_DUE, nextDue);
        if (roomId > 0) values.put(COL_CHORE_ROOM_FK, roomId); else values.putNull(COL_CHORE_ROOM_FK);
        getWritableDatabase().insertWithOnConflict(
                TABLE_CHORES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Returns false if the new name conflicts with another of this user's chores. */
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

    public ArrayList<Chore> loadChores(int userId) {
        ArrayList<Chore> chores = new ArrayList<>();
        String sql = "SELECT c." + COL_ID + ", c." + COL_NAME + ", c." + COL_FREQUENCY +
                ", c." + COL_NEXT_DUE + ", r." + COL_ROOM_ID + ", r." + COL_ROOM_NAME +
                ", r." + COL_ROOM_EMOJI +
                " FROM " + TABLE_CHORES + " c" +
                " LEFT JOIN " + TABLE_ROOMS + " r ON c." + COL_CHORE_ROOM_FK + " = r." + COL_ROOM_ID +
                " WHERE c." + COL_CHORE_USER_FK + " = ?" +
                " ORDER BY c." + COL_NAME + " ASC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
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

    // ── Home Profile ──────────────────────────────────────────────────────────

    public void saveProfile(HomeProfile profile, int userId) {
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_USER, userId);
        values.put(COL_HOME_TYPE, profile.homeType);
        values.put(COL_BEDROOMS, profile.bedrooms);
        values.put(COL_BATHROOMS, profile.bathrooms);
        values.put(COL_LAUNDRY, profile.laundryType);
        values.put(COL_HOUSEHOLD, profile.householdMembers);
        values.put(COL_CLEANING_STYLE, profile.cleaningStyle);
        values.put(COL_PAIN_POINTS, profile.painPoints);
        // CONFLICT_REPLACE upserts by PK (UserId) — safe to call multiple times
        getWritableDatabase().insertWithOnConflict(
                TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public HomeProfile loadProfile(int userId) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_PROFILE, null,
                COL_PROFILE_USER + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            HomeProfile p = new HomeProfile();
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

    public boolean hasProfile(int userId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PROFILE + " WHERE " + COL_PROFILE_USER + "=?",
                new String[]{String.valueOf(userId)});
        boolean exists = cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // ── Reset (user data only — account row is preserved) ────────────────────

    public void resetAll(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        // Chore deletions cascade to CompletionTable via FK
        db.delete(TABLE_CHORES, COL_CHORE_USER_FK + "=?", new String[]{String.valueOf(userId)});
        db.delete(TABLE_PROFILE, COL_PROFILE_USER + "=?", new String[]{String.valueOf(userId)});
    }
}
