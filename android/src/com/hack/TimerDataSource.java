package com.hack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hack.HackDbContract.HackTimers;

public class TimerDataSource extends HackDataSource {

    /**
     * Ctor
     */
    public TimerDataSource(Context context) {
        super(context);
    }

    /**
     * Add a new timer
     * @return long - id of new timer
     */
    public long addTimer(long deviceId, String timeOn, String timeOff, boolean isRepeated) {
        ContentValues values = new ContentValues();
        values.put(HackTimers.COLUMN_NAME_DEVICE_ID, deviceId);
        values.put(HackTimers.COLUMN_NAME_TIMER_TIME_ON, timeOn);
        values.put(HackTimers.COLUMN_NAME_TIMER_TIME_OFF, timeOff);
        values.put(HackTimers.COLUMN_NAME_TIMER_IS_REPEATED, (isRepeated) ? 1 : 0);
        long id = mDatabase.insert(HackTimers.TABLE_NAME, null, values);
        return id;
    }

    /**
     * Delete a timer
     * @return int - number of rows deleted
     */
    public int deleteTimerByDeviceId(long deviceId) {
        int result = mDatabase.delete(
                HackTimers.TABLE_NAME,
                HackTimers.COLUMN_NAME_DEVICE_ID + " = " + deviceId,
                null);
        return result;
    }

    /**
     * Retrieve a timer
     * @return Timer or null if no Timer exists with given device id
     */
    public Timer getTimerByDeviceId(long deviceId) {
        Cursor cursor = mDatabase.query(
                HackTimers.TABLE_NAME,
                mColumns.toArray(new String[mColumns.size()]), 
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
