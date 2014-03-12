package com.hack;

import com.hack.HackDbContract.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HackDbHelper extends SQLiteOpenHelper {
    
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hack.db";
    
    private static final String TEXT_TYPE = "TEXT";
    private static final String INT_TYPE = "INT";
    private static final String SQL_CREATE_HARDWARE_UNITS =
        "CREATE TABLE " + HackHardwareUnits.TABLE_NAME + 
        " (" +
            HackHardwareUnits._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME + " " + TEXT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH + " " + TEXT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER + " " + INT_TYPE +
        ")";
    
    private static final String SQL_INSERT_HARDWARE_UNTIS =
            "INSERT INTO " + HackHardwareUnits.TABLE_NAME + 
            " (" + 
                HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME + ", " +
                HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH + ", " +
                HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER + 
            ") " +
            "VALUES " + "('Test Unit', 'http://brandonnoad.com/', 80)";
    
    private static final String SQL_CREATE_SOCKETS =
        "CREATE TABLE " + HackSockets.TABLE_NAME + 
        " (" +
            HackSockets._ID + " INTEGER PRIMARY KEY, " +
            HackSockets.COLUMN_NAME_SOCKET_DESCRIPTION + " " + TEXT_TYPE + 
        ")";
    
    private static final String SQL_INSERT_SOCKETS =
        "INSERT INTO " + HackSockets.TABLE_NAME + " (" + HackSockets.COLUMN_NAME_SOCKET_DESCRIPTION + ")" +
        " VALUES " + "('NW'), ('NE'), ('SW'), ('SE')";
    
    private static final String SQL_CREATE_DEVICES =
        "CREATE TABLE " + HackDevices.TABLE_NAME + 
        " (" +
            HackDevices._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            HackDevices.COLUMN_NAME_HARDWARE_UNIT_ID + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_SOCKET_ID + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_NAME + " " + TEXT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_STATE + " " + INT_TYPE +
        ")";    

    private static final String SQL_DELETE_HARDWARE_UNITS =
        "DROP TABLE IF EXISTS " + HackHardwareUnits.TABLE_NAME;
    
    private static final String SQL_DELETE_SOCKETS =
            "DROP TABLE IF EXISTS " + HackSockets.TABLE_NAME;
    
    private static final String SQL_DELETE_DEVICES =
            "DROP TABLE IF EXISTS " + HackDevices.TABLE_NAME;

    public HackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
        
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_HARDWARE_UNITS);
        db.execSQL(SQL_CREATE_SOCKETS);
        db.execSQL(SQL_CREATE_DEVICES);
        db.execSQL(SQL_INSERT_HARDWARE_UNTIS);
        db.execSQL(SQL_INSERT_SOCKETS);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database's upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_HARDWARE_UNITS);
        db.execSQL(SQL_DELETE_SOCKETS);
        db.execSQL(SQL_DELETE_DEVICES);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
