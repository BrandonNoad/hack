package com.hack;

import com.hack.HackDbContract.HackDeviceTypes;
import com.hack.HackDbContract.HackDevices;
import com.hack.HackDbContract.HackTimers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TimerDataSource {
    
 // Database fields
    private SQLiteDatabase mDatabase;
    private HackDbHelper mDbHelper;
    private String[] mAllColumns = { 
            "*"
    };
      
    public TimerDataSource(Context context) {
      mDbHelper = new HackDbHelper(context);
    }

    public void open() throws SQLException {
      mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
      mDbHelper.close();
    }
    
    public long addTimer(long deviceId, String timeOn, String timeOff, boolean isRepeated) {
        ContentValues values = new ContentValues();
        values.put(HackTimers.COLUMN_NAME_DEVICE_ID, deviceId);
        values.put(HackTimers.COLUMN_NAME_TIMER_TIME_ON, timeOn);
        values.put(HackTimers.COLUMN_NAME_TIMER_TIME_OFF, timeOff);
        values.put(HackTimers.COLUMN_NAME_TIMER_IS_REPEATED, (isRepeated) ? 1 : 0);
        long id = mDatabase.insert(HackTimers.TABLE_NAME, null, values);
        return id;
    }
    
    public int deleteTimerByDeviceId(long deviceId) {
        int result = mDatabase.delete(
                HackTimers.TABLE_NAME,
                HackTimers.COLUMN_NAME_DEVICE_ID + " = " + deviceId,
                null);
        return result;
    }
    
    public Timer getTimerByDeviceId(long deviceId) {
        Cursor cursor = mDatabase.query(
                HackTimers.TABLE_NAME,
                mAllColumns, 
                HackTimers.COLUMN_NAME_DEVICE_ID + " = " + deviceId,
                null, 
                null, 
                null, 
                null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            long devId = cursor.getLong(1);            
            String timeOn = cursor.getString(2);
            String timeOff = cursor.getString(3);
            int isRepeated = cursor.getInt(4);
            Timer t = new Timer(id, devId, timeOn, timeOff, (isRepeated == 1) ? true : false);
            return t;
        } else {
            return null;
        }
    }

}
