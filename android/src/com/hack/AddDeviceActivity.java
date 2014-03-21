package com.hack;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddDeviceActivity extends Activity {
    
    public final static String EXTRA_UNIT_ID = "com.hack.UNIT_ID";
    
    private DeviceDataSource mDeviceDataSource;
    private long mHardwareUnitId;
    private long mSocketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        
        EditText deviceNameET = (EditText) findViewById(R.id.deviceNameEditText);
        
        deviceNameET.setOnEditorActionListener(new OnEditorActionListener(){
           @Override
           public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               if (actionId == EditorInfo.IME_ACTION_DONE) {
                   addDevice();
                   return true;
               }
               return false;
           }
        });
        
        //create spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        
        //populate spinner with predefined types listed in hardware_types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.hardware_types, android.R.layout.simple_spinner_item);
        
        //set layout of spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        Intent intent = getIntent();
        String parentName = intent.getStringExtra("parent");
        
        //This means we're coming from Device Details screen
        if(parentName.equals("DeviceDetailsActivity")){
        	
        	//update action bar title
        	setTitle("EditDevice");
        	
        	//set EditText field to be the name of the device
        	deviceNameET.setText("someName");
        	
        	//set default hardware type on spinner to be the type of the device
        	
        }
        
        mHardwareUnitId = intent.getLongExtra(SingleUnitActivity.EXTRA_HARDWARE_UNIT_ID, -1);
        mSocketId = intent.getLongExtra(SingleUnitActivity.EXTRA_SOCKET_ID, -1);        
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //this is called when the "save" button is clicked
    public void saveNewDevice(View view){}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_unit, menu);
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
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra(AllUnitsActivity.EXTRA_UNIT_ID, mHardwareUnitId);            
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addDevice() {
        Intent intent = new Intent(this, SingleUnitActivity.class);
        EditText et = (EditText) findViewById(R.id.deviceNameEditText);
        String deviceName = et.getText().toString();
        long deviceId = mDeviceDataSource.addDevice(mHardwareUnitId, mSocketId, deviceName);
        intent.putExtra(EXTRA_UNIT_ID, mHardwareUnitId);  // pass back hardware unit id
        startActivity(intent);
    }
}
