package com.hack;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hack.HackDbContract.HackDevices;
import com.hack.HackDbContract.HackHardwareUnits;

public class HardwareUnitDataSource {
    
    // Database fields
    private SQLiteDatabase mDatabase;
    private HackDbHelper mDbHelper;
    private String[] mAllColumns = { 
            "_ID",
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME,
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH,
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER
    };
      
    public HardwareUnitDataSource(Context context) {
      mDbHelper = new HackDbHelper(context);
    }

    public void open() throws SQLException {
      mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
      mDbHelper.close();
    }

    public long addHardwareUnit(String name, String basePath, int portNumber) {
      ContentValues values = new ContentValues();
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME, name);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_BASE_PATH, basePath);
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER, portNumber);
      long id = mDatabase.insert(HackHardwareUnits.TABLE_NAME, null, values);
      return id;
    }
    
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
              mAllColumns, 
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
        HardwareUnit hu = new HardwareUnit(id, name, basePath, portNumber);
        hardwareUnits.add(hu);
        cursor.moveToNext();
      }
      // make sure to close the cursor
      cursor.close();
      return hardwareUnits;
    }
    
    public HardwareUnit getHardwareUnitById(long id) {
        Cursor cursor = mDatabase.query(
                HackHardwareUnits.TABLE_NAME,
                mAllColumns, 
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
        HardwareUnit hu = new HardwareUnit(huId, name, basePath, portNumber);
        return hu;        
    }

}