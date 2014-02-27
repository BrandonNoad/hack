package com.hack;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class AllUnitsActivity extends Activity {
    
    public final static String EXTRA_UNIT_ID = "com.hack.UNIT_ID";
    
    private HardwareUnitAdapter mHardwareUnitAdapter;
    private ArrayList<HardwareUnit> mAllUnitsList = new ArrayList<HardwareUnit>(); 
    private HardwareUnitsDataSource mHardwareUnitsDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_units);
        
        mHardwareUnitsDataSource = new HardwareUnitsDataSource(this);
        mHardwareUnitsDataSource.open();
        
        mAllUnitsList = mHardwareUnitsDataSource.getAllHardwareUnits();
        
        Button addUnitButton = (Button) findViewById(R.id.addUnit);
        addUnitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addHardwareUnit();
            }
        });
        
        mHardwareUnitAdapter = new HardwareUnitAdapter(this, mAllUnitsList);
        
        ListView listOfAllHardware = (ListView) findViewById(R.id.listOfAllHardware);
        listOfAllHardware.setAdapter(mHardwareUnitAdapter);
        
        listOfAllHardware.setOnItemClickListener(new OnItemClickListener() {
           public void onItemClick(AdapterView parent, View v, int position, long id) {
               HardwareUnit unit = (HardwareUnit) parent.getItemAtPosition(position);
               startSingleUnitActivity(unit.getId());
           }
        });
        
//        Intent intent = getIntent();
//        long unitId = intent.getLongExtra(AddUnitActivity.EXTRA_UNIT_ID, -1);
//        
//        if (unitId >= 0) {
//            Log.i("All", "Adding new unit");
//            HardwareUnit unit = mHardwareUnitsDataSource.getHardwareUnitById(unitId);
//            mHardwareUnitAdapter.add(unit);
//        } else {
//            Log.i("All", "Failed to add new unit");
//        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.all_hardware, menu);
        return true;
    }
    
    public void addHardwareUnit() {
        Intent intent = new Intent(this, AddUnitActivity.class);
        startActivity(intent);
    }
    
    public void startSingleUnitActivity(long unitId) {
        Intent intent = new Intent(this, SingleUnitActivity.class);
        intent.putExtra(EXTRA_UNIT_ID, unitId);
        startActivity(intent);
    }

}
