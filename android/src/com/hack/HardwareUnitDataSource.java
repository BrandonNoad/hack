package com.hack;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hack.HackDbContract.HackHardwareUnits;

public class HardwareUnitDataSource extends HackDataSource {
    
    /**
     * Ctor
     */
    public HardwareUnitDataSource(Context context) {
        super(context);
    }
    
    /**
     * Add a new hardware unit
     * @return long - id of the new hardware unit
     */
    public long addHardwareUnit(String name, String basePath, int portNumber, String accessPointName, String wpa2Key, String btMac) {
      ContentValues values = new ContentValues();
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME, name);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH, basePath);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER, portNumber);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_ACCESS_POINT_NAME, accessPointName);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_WPA2_KEY, wpa2Key);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BT_MAC, btMac);
      long id = mDatabase.insert(HackHardwareUnits.TABLE_NAME, null, values);
      return id;
    }
    
    /**
     * Delete a hardware unit given an id
     * @return int - number of rows deleted
     */
    public int deleteHardwareUnitById(long huId) {
        int result = mDatabase.delete(
                HackHardwareUnits.TABLE_NAME,
                HackHardwareUnits._ID + " = " + huId,
                null);
        return result;
    }
    
    public ArrayList<HardwareUnit> getAllHardwareUnits() {
      ArrayList<HardwareUnit> hardwareUnits = new ArrayList<HardwareUnit>();     
      Cursor cursor = mDatabase.query(
              HackHardwareUnits.TABLE_NAME,
              mColumns.toArray(new String[mColumns.size()]), 
              null, 
              null, 
              null, 
              null, 
              null);

      cursor.moveToFirst();
      while (!cursor.isAfterLast()) {
        long id = cursor.getLong(0);
        String name = cursor.getString(1);
        String basePath = cursor.getString(2);
        int portNumber = cursor.getInt(3);
        String accessPointName = cursor.getString(4);
        String wpa2Key = cursor.getString(5);
        String btMac = cursor.getString(6);
        HardwareUnit hu = new HardwareUnit(id, name, basePath, portNumber, accessPointName, wpa2Key, btMac);
        hardwareUnits.add(hu);
        cursor.moveToNext();
      }
      // make sure to close the cursor
      cursor.close();
      return hardwareUnits;
    }
    
    /**
     * Retrieve a hardware unit given an id
     * @return HardwareUnit; null if there is no hardware unit with the given id
     */
    public HardwareUnit getHardwareUnitById(long id) {
        Cursor cursor = mDatabase.query(
                HackHardwareUnits.TABLE_NAME,
                mColumns.toArray(new String[mColumns.size()]), 
                HackHardwareUnits._ID + "= " + id, 
                null, 
                null, 
                null, 
                null);

        cursor.moveToFirst();
        long huId = cursor.getLong(0);
        String name = cursor.getString(1);
        String basePath = cursor.getString(2);
        int portNumber = cursor.getInt(3);
        String accessPointName = cursor.getString(4);
        String wpa2Key = cursor.getString(5);
        String btMac = cursor.getString(6);
        HardwareUnit hu = new HardwareUnit(id, name, basePath, portNumber, accessPointName, wpa2Key, btMac);
        return hu;
    }

}