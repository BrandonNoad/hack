package com.hack;

import java.util.ArrayList;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HackDataSource {

    // manages on-device SQLite db
    protected SQLiteDatabase mDatabase;
    // manages db creation and version management
    protected HackDbHelper mDbHelper;
    // list of all the columns you want to return in SELECT queries
    protected ArrayList<String> mColumns = new ArrayList<String>();

    /**
     * Ctor
     */
    public HackDataSource(Context context) {
        mDbHelper = new HackDbHelper(context);
        // select all columns by default
        mColumns.add("*"); 
    }

    /**
     * Open database
     * @throws SQLException
     */
    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    /**
     * Close database
     */
    public void close() {
        mDbHelper.close();
    }

}
