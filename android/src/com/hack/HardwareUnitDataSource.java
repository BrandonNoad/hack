package com.hack;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hack.HackDbContract.HackHardwareUnits;

public class HardwareUnitDataSource {
    
    // Database fields
    private SQLiteDatabase mDatabase;
    private HackDbHelper mDbHelper;
    private String[] mAllColumns = { 
            "_ID",
            HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME
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

    public long addHardwareUnit(String name) {
      ContentValues values = new ContentValues();
      values.put(HackHardwareUnits.COLUMN_NAME_HARDWARE_UNIT_NAME, name);
      long id = mDatabase.insert(HackHardwareUnits.TABLE_NAME, null, values);
      return id;
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
        HardwareUnit hu = new HardwareUnit(id, name);
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
        HardwareUnit hu = new HardwareUnit(huId, name);
        return hu;
        
    }

}