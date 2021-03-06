package com.hack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hack.HackDbContract.HackDeviceTypes;
import com.hack.HackDbContract.HackDevices;
import com.hack.HackDbContract.HackHardwareUnits;
import com.hack.HackDbContract.HackSockets;
import com.hack.HackDbContract.HackTimers;

public class HackDbHelper extends SQLiteOpenHelper {

    // db version number
    public static final int DATABASE_VERSION = 1;
    // db name
    public static final String DATABASE_NAME = "hack.db";

    // constants used in queries
    private static final String TEXT_TYPE = "TEXT";
    private static final String INT_TYPE = "INT";

    // create hardwareUnits table
    private static final String SQL_CREATE_HARDWARE_UNITS =
            "CREATE TABLE " + HackHardwareUnits.TABLE_NAME + 
            " (" +
            HackHardwareUnits._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME + " " + TEXT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH + " " + TEXT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER + " " + INT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_ACCESS_POINT_NAME + " " + TEXT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_WPA2_KEY + " " + TEXT_TYPE + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BT_MAC + " " + TEXT_TYPE  +
            ")";

    // add a hardwareUnit for testing purposes
    // TODO: remove this
    private static final String SQL_INSERT_HARDWARE_UNTIS =
            "INSERT INTO " + HackHardwareUnits.TABLE_NAME + 
            " (" + 
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_ACCESS_POINT_NAME + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_WPA2_KEY + ", " +
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BT_MAC +
            ") " +
            "VALUES " + "('Test Unit', 'brandonnoad.com', 80, 'bbnoad', 'wpa2Key', 'BTMAC')";

    // create sockets table
    private static final String SQL_CREATE_SOCKETS =
            "CREATE TABLE " + HackSockets.TABLE_NAME + 
            " (" +
            HackSockets._ID + " INTEGER PRIMARY KEY, " +
            HackSockets.COLUMN_NAME_SOCKET_DESCRIPTION + " " + TEXT_TYPE + 
            ")";

    // insert 4 sockets
    private static final String SQL_INSERT_SOCKETS =
            "INSERT INTO " + HackSockets.TABLE_NAME + " (" + HackSockets.COLUMN_NAME_SOCKET_DESCRIPTION + ")" +
                    " VALUES " + "('NW'), ('NE'), ('SW'), ('SE')";

    // create deviceTypes table
    private static final String SQL_CREATE_DEVICE_TYPES =
            "CREATE TABLE " + HackDeviceTypes.TABLE_NAME + 
            " (" +
            HackDeviceTypes._ID + " INTEGER PRIMARY KEY, " +
            HackDeviceTypes.COLUMN_NAME_DEVICE_TYPE_NAME + " " + TEXT_TYPE + 
            ")";

    // insert 3 deviceTypes - Simple, Light, Heat/Cool
    private static final String SQL_INSERT_DEVICE_TYPES =
            "INSERT INTO " + HackDeviceTypes.TABLE_NAME + " (" + HackDeviceTypes.COLUMN_NAME_DEVICE_TYPE_NAME + ")" +
                    " VALUES " + "('Simple'), ('Light'), ('Heat/Cool')";

    // create devices table
    private static final String SQL_CREATE_DEVICES =
            "CREATE TABLE " + HackDevices.TABLE_NAME + 
            " (" +
            HackDevices._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            HackDevices.COLUMN_NAME_HARDWARE_UNIT_ID + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_SOCKET_ID + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_NAME + " " + TEXT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_STATE + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_TYPE_ID + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_TOTAL_TIME_ON + " " + INT_TYPE + ", " +
            HackDevices.COLUMN_NAME_DEVICE_ON_SINCE_TIME + " " + INT_TYPE +            
            ")";

    // create timers table
    private static final String SQL_CREATE_TIMERS =
            "CREATE TABLE " + HackTimers.TABLE_NAME + 
            " (" +
            HackTimers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            HackTimers.COLUMN_NAME_DEVICE_ID + " " + INT_TYPE + ", " +
            HackTimers.COLUMN_NAME_TIMER_TIME_ON + " " + INT_TYPE + ", " +
            HackTimers.COLUMN_NAME_TIMER_TIME_OFF + " " + INT_TYPE + ", " +
            HackTimers.COLUMN_NAME_TIMER_IS_REPEATED + " " + INT_TYPE +
            ")";

    // delete hardwareUnits    
    private static final String SQL_DELETE_HARDWARE_UNITS =
            "DROP TABLE IF EXISTS " + HackHardwareUnits.TABLE_NAME;

    // delete sockets
    private static final String SQL_DELETE_SOCKETS =
            "DROP TABLE IF EXISTS " + HackSockets.TABLE_NAME;

    // delete deviceTypes
    private static final String SQL_DELETE_DEVICE_TYPES =
            "DROP TABLE IF EXISTS " + HackDeviceTypes.TABLE_NAME;

    // delete devices
    private static final String SQL_DELETE_DEVICES =
            "DROP TABLE IF EXISTS " + HackDevices.TABLE_NAME;

    // delete timers
    private static final String SQL_DELETE_TIMERS =
            "DROP TABLE IF EXISTS " + HackDevices.TABLE_NAME;

    /**
     * Ctor
     */
    public HackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create db
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_HARDWARE_UNITS);
        db.execSQL(SQL_CREATE_SOCKETS);
        db.execSQL(SQL_CREATE_DEVICE_TYPES);
        db.execSQL(SQL_CREATE_DEVICES);
        db.execSQL(SQL_CREATE_TIMERS);
//        db.execSQL(SQL_INSERT_HARDWARE_UNTIS);
        db.execSQL(SQL_INSERT_SOCKETS);
        db.execSQL(SQL_INSERT_DEVICE_TYPES);
    }

    // upgrade db
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database's upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_HARDWARE_UNITS);
        db.execSQL(SQL_DELETE_SOCKETS);
        db.execSQL(SQL_DELETE_DEVICE_TYPES);
        db.execSQL(SQL_DELETE_DEVICES);
        db.execSQL(SQL_DELETE_TIMERS);
        onCreate(db);
    }

    // downgrade db
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
