package com.annabenson.choretracker;

import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    private static final int DATABASE_VERSION = 1; //
    private static final String DATABASE_NAME = "ChoreAppDB";
    private static final String TABLE_NAME = "ChoreAppTable";

    // columns
    private static final String NAME = "ChoreName";
    private static final String FREQUENCY = "ChoreFrequency";
    private static final String DATES = "LastTimeDone";
    private static final String
}
