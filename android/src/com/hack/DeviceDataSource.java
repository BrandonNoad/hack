package com.hack;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hack.HackDbContract.HackDeviceTypes;
import com.hack.HackDbContract.HackDevices;
import com.hack.HackDbContract.HackHardwareUnits;

public class DeviceDataSource extends HackDataSource {
    
    /**
     * Ctor
     */
    public DeviceDataSource(Context context) {
        super(context);
    }

    /**
     * Add a new device to the database
     * @return long - new device's id
     */
    public long addDevice(long hardwareUnitId, long socketId, String name, long deviceTypeId) {
      ContentValues values = new ContentValues();
      values.put(HackDevices.COLUMN_NAME_HARDWARE_UNIT_ID, hardwareUnitId);
      values.put(HackDevices.COLUMN_NAME_SOCKET_ID, socketId);
      values.put(HackDevices.COLUMN_NAME_DEVICE_NAME, name);
      values.put(HackDevices.COLUMN_NAME_DEVICE_STATE, 1);  // 1 is off on the espruino
      values.put(HackDevices.COLUMN_NAME_DEVICE_TYPE_ID, deviceTypeId);
      values.put(HackDevices.COLUMN_NAME_DEVICE_TOTAL_TIME_ON, 0);
      long id = mDatabase.insert(HackDevices.TABLE_NAME, null, values);
      return id;
    }
    
    /**
     * Update an existing device
     * @return int result - the number of rows updated
     */
    public int updateDevice(long deviceId, int state) {
        ContentValues values = new ContentValues();
        values.put(HackDevices.COLUMN_NAME_DEVICE_STATE, state);
        int result = mDatabase.update(HackDevices.TABLE_NAME, values, HackDevices._ID + " = " + deviceId, null);
        return result;
    }
    
    /**
     * Update an existing device
     * @return int result - the number of rows updated
     */
    public int updateDevice(long deviceId, String name, long deviceTypeId) {
        ContentValues values = new ContentValues();
        values.put(HackDevices.COLUMN_NAME_DEVICE_NAME, name);
        values.put(HackDevices.COLUMN_NAME_DEVICE_TYPE_ID, deviceTypeId);
        int result = mDatabase.update(HackDevices.TABLE_NAME, values, HackDevices._ID + " = " + deviceId, null);
        return result;
    }
    
    
    /**
     * Delete a device from the database
     * @return int - # of rows deleted
     */
    public int deleteDeviceById(long deviceId) {
        int result = mDatabase.delete(
                HackDevices.TABLE_NAME,
                HackDevices._ID + " = " + deviceId,
                null);
        return result;
    }
    
    /**
     * Retrieve a device given an id
     * @return Device - the Device matching the id or null if no device with
     *  given id.
     */
    public Device getDeviceById(long id) {
        Cursor cursor = mDatabase.query(
                HackDevices.TABLE_NAME + " LEFT OUTER JOIN " + HackDeviceTypes.TABLE_NAME + " ON (" + HackDevices.TABLE_NAME + ".deviceTypeId = " + HackDeviceTypes.TABLE_NAME + "._ID)",
                mColumns.toArray(new String[mColumns.size()]), 
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
    
    /**
     * Retrieve a device given a hardware id and a socket id
     * @return Device - the Device matching the ids or null if no such device exists
     */
    public Device getDevice(long hId, long sId) {
        Cursor cursor = mDatabase.query(
                HackDevices.TABLE_NAME + " LEFT OUTER JOIN " + HackDeviceTypes.TABLE_NAME + " ON (" + HackDevices.TABLE_NAME + ".deviceTypeId = " + HackDeviceTypes.TABLE_NAME + "._ID)",
                mColumns.toArray(new String[mColumns.size()]), 
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
    
    public ArrayList<Device> getAllDevicesForHardwareUnit(long hardwareUnitId) {
        ArrayList<Device> devices = new ArrayList<Device>();     
        Cursor cursor = mDatabase.query(
                HackDevices.TABLE_NAME + " LEFT OUTER JOIN " + HackDeviceTypes.TABLE_NAME + " ON (" + HackDevices.TABLE_NAME + ".deviceTypeId = " + HackDeviceTypes.TABLE_NAME + "._ID)",
                mColumns.toArray(new String[mColumns.size()]), 
                HackDevices.COLUMN_NAME_HARDWARE_UNIT_ID + " = " + hardwareUnitId, 
                null, 
                null, 
                null, 
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long deviceId = cursor.getLong(0);
            long huId = cursor.getLong(1);
            long socketId = cursor.getLong(2);
            String name = cursor.getString(3);
            int state = cursor.getInt(4);
            long deviceTypeId = cursor.getLong(5);
            int totalTimeOn = cursor.getInt(6);
            String type = cursor.getString(8);
            Device d = new Device(deviceId, huId, socketId, name, state, type, totalTimeOn);
            devices.add(d);
          cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return devices;
    }

}
