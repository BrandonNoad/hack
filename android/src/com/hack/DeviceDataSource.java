package com.hack;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hack.HackDbContract.HackDeviceTypes;
import com.hack.HackDbContract.HackDevices;
import com.hack.HackDbContract.HackHardwareUnits;

public class DeviceDataSource {
    
    // Database fields
    private SQLiteDatabase mDatabase;
    private HackDbHelper mDbHelper;
    private String[] mAllColumns = { 
            "*"
    };
      
    public DeviceDataSource(Context context) {
      mDbHelper = new HackDbHelper(context);
    }

    public void open() throws SQLException {
      mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
      mDbHelper.close();
    }

    public long addDevice(long hardwareUnitId, long socketId, String name, long deviceTypeId) {
      ContentValues values = new ContentValues();
      values.put(HackDevices.COLUMN_NAME_HARDWARE_UNIT_ID, hardwareUnitId);
      values.put(HackDevices.COLUMN_NAME_SOCKET_ID, socketId);
      values.put(HackDevices.COLUMN_NAME_DEVICE_NAME, name);
      values.put(HackDevices.COLUMN_NAME_DEVICE_STATE, 0);
      values.put(HackDevices.COLUMN_NAME_DEVICE_TYPE_ID, deviceTypeId);
      values.put(HackDevices.COLUMN_NAME_DEVICE_TOTAL_TIME_ON, 0);
      long id = mDatabase.insert(HackDevices.TABLE_NAME, null, values);
      return id;
    }
    
    public int deleteDeviceById(long deviceId) {
        int result = mDatabase.delete(
                HackDevices.TABLE_NAME,
                HackDevices._ID + " = " + deviceId,
                null);
        return result;
    }
    
//    public ArrayList<Device> getAllDevices() {
//      ArrayList<Device> devices = new ArrayList<Device>();
//      Cursor cursor = mDatabase.query(
//              HackDevices.TABLE_NAME,
//              mAllColumns, 
//              null, 
//              null, 
//              null, 
//              null, 
//              null);
//
//      cursor.moveToFirst();
//      while (!cursor.isAfterLast()) {
//        long id = cursor.getLong(0);
//        long hardwareUnitId = cursor.getLong(1);
//        long socketId = cursor.getLong(2);
//        String name = cursor.getString(3);
//        int state = cursor.getInt(4);
//        Device d = new Device(id, hardwareUnitId, socketId, name, state);
//        devices.add(d);
//        cursor.moveToNext();
//      }
//      // make sure to close the cursor
//      cursor.close();
//      return devices;
//    }
    
    public Device getDeviceById(long id) {
        Cursor cursor = mDatabase.query(
                HackDevices.TABLE_NAME + " LEFT OUTER JOIN " + HackDeviceTypes.TABLE_NAME + " ON (" + HackDevices.TABLE_NAME + ".deviceTypeId = " + HackDeviceTypes.TABLE_NAME + "._ID)",
                mAllColumns, 
                HackDevices.TABLE_NAME + "." + HackDevices._ID + " = " + id,
                null, 
                null, 
                null, 
                null);

        if (cursor.moveToFirst()) {
            long deviceId = cursor.getLong(0);
            long hardwareUnitId = cursor.getLong(1);
            long socketId = cursor.getLong(2);
            String name = cursor.getString(3);
            int state = cursor.getInt(4);
            long deviceTypeId = cursor.getLong(5);
            int totalTimeOn = cursor.getInt(6);
            String type = cursor.getString(8);
            Device d = new Device(deviceId, hardwareUnitId, socketId, name, state, type, totalTimeOn);
            return d;
        } else {
            return null;
        }
    }
    
    public Device getDevice(long hId, long sId) {
        Cursor cursor = mDatabase.query(
                HackDevices.TABLE_NAME + " LEFT OUTER JOIN " + HackDeviceTypes.TABLE_NAME + " ON (" + HackDevices.TABLE_NAME + ".deviceTypeId = " + HackDeviceTypes.TABLE_NAME + "._ID)",
                mAllColumns, 
                HackDevices.COLUMN_NAME_HARDWARE_UNIT_ID + " = " + hId + " " +
                "AND " + HackDevices.COLUMN_NAME_SOCKET_ID + " = " + sId,
                null, 
                null, 
                null, 
                null);

        if (cursor.moveToFirst()) {
            long deviceId = cursor.getLong(0);
            long hardwareUnitId = cursor.getLong(1);
            long socketId = cursor.getLong(2);
            String name = cursor.getString(3);
            int state = cursor.getInt(4);
            long deviceTypeId = cursor.getLong(5);
            int totalTimeOn = cursor.getInt(6);
            String type = cursor.getString(8);
            Device d = new Device(deviceId, hardwareUnitId, socketId, name, state, type, totalTimeOn);
            return d;
        } else {
            return null;
        }
    }

}
