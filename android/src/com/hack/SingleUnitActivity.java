package com.hack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SingleUnitActivity extends Activity {
    
    public final static String EXTRA_SOCKET_ID = "com.hack.SOCKET_ID";
    private HardwareUnitDataSource mHardwareUnitsDataSource;
    
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            int buttonId = v.getId();
            long socketId = -1;
            switch(buttonId) {
                case (R.id.socket0):
                    socketId = 0;
                    break;
                case (R.id.socket1):
                    socketId = 1;
                    break;
                case (R.id.socket2):
                    socketId = 2;
                    break;
                case (R.id.socket3):
                    socketId = 3;
                    break;
                default:
                    break;            
            }
            startDeviceDetailsActivity(socketId);
        }
    };     
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_unit);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mHardwareUnitsDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitsDataSource.open();
        
        Button socketButton0 = (Button) findViewById(R.id.socket0);
        socketButton0.setOnClickListener(mOnClickListener);        
        Button socketButton1 = (Button) findViewById(R.id.socket1);
        socketButton1.setOnClickListener(mOnClickListener);
        Button socketButton2 = (Button) findViewById(R.id.socket2);
        socketButton2.setOnClickListener(mOnClickListener);
        Button socketButton3 = (Button) findViewById(R.id.socket3);
        socketButton3.setOnClickListener(mOnClickListener);
        
        Intent intent = getIntent();
        Long unitId = intent.getLongExtra(AllUnitsActivity.EXTRA_UNIT_ID, -1);        
        displayUnit(unitId);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_unit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void displayUnit(long unitId) {
        HardwareUnit unit = mHardwareUnitsDataSource.getHardwareUnitById(unitId);
        TextView unitName = (TextView) findViewById(R.id.unitName);
        unitName.setText(unit.getName());
    }
    
    public void startDeviceDetailsActivity(long socketId) {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(EXTRA_SOCKET_ID, socketId);
        startActivity(intent);
    }

}
