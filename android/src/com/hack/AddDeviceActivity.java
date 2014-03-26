package com.hack;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddDeviceActivity extends Activity {
    
    // -- Constants
    
    public final static String EXTRA_UNIT_ID = "com.hack.UNIT_ID";
    
    // -- Member Variables
    
    private DeviceDataSource mDeviceDataSource;
    private long mHardwareUnitId;
    private long mSocketId;
    private long mDeviceId;
    private Device mDevice = null;
    
    private Spinner mDeviceTypeSpinner;
    private ArrayAdapter<CharSequence> mDeviceTypeAdapter;
    private int mSelectedDeviceTypeId = 1;
    private ActionBar mActionBar;
    private EditText deviceNameET;

    // -- Initialize Activity
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        // Show the Up button in the action bar.
        setupActionBar();
        
        // initialize memebrs
        mActionBar = getActionBar();   
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        deviceNameET = (EditText) findViewById(R.id.deviceNameEditText);
        
        
        // get hardware unit id and socket id from previous activity
        Intent intent = getIntent();
        
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        mHardwareUnitId = intent.getLongExtra(SingleUnitActivity.EXTRA_HARDWARE_UNIT_ID, -1);
        mSocketId = intent.getLongExtra(SingleUnitActivity.EXTRA_SOCKET_ID, -1);
        String title = intent.getStringExtra(SingleUnitActivity.EXTRA_TITLE);
        
        mActionBar.setTitle(title);
        
        // create spinner
        mDeviceTypeSpinner = (Spinner) findViewById(R.id.deviceTypeSpinner);
        
        //populate spinner with predefined types listed in hardware_types
        mDeviceTypeAdapter = ArrayAdapter.createFromResource(this,
                                                             R.array.hardware_types, 
                                                             android.R.layout.simple_spinner_item);
        
        // set layout of spinner
        mDeviceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDeviceTypeSpinner.setAdapter(mDeviceTypeAdapter);
        
        if (mDeviceId != -1) {
            mDevice = mDeviceDataSource.getDeviceById(mDeviceId);
            if (mDevice != null) {
                // fill in device name
                EditText deviceName = (EditText) findViewById(R.id.deviceNameEditText);
                deviceName.setText(mDevice.getName());
                deviceName.setSelection(mDevice.getName().length());
                // change spinner selection
                mDeviceTypeSpinner.setSelection(mDeviceTypeAdapter.getPosition(mDevice.getType()));
            }
            
        }
        
        
        
        // set up event listeners
        deviceNameET.setOnEditorActionListener(new OnEditorActionListener(){
           @Override
           public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               if (actionId == EditorInfo.IME_ACTION_DONE) {
                   if (mDeviceId != -1) {
                       updateDevice();
                       startDeviceDetailsActivity(mDeviceId);
                   } else {
                       addDevice();
                       startSingleUnitActivity();
                   }
                   
                   return true;
               }
               return false;
           }
        });
        
        // set spinner event listener
        mDeviceTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int pos, long id) {
                /* This is a bit of a hack, but should work for now since:
                 *  position 0 == Simple == deviceTypeId 1
                 *  position 1 == Light == deviceTypeId 2
                 *  position 2 == Heat/Cool == deviceTypeId 3
                 */
                mSelectedDeviceTypeId = pos + 1;                
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // default to simple
                mSelectedDeviceTypeId = 1;
                
            }});
        
           
        Button addDeviceButton = (Button) findViewById(R.id.add_device_button);
        addDeviceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
          
            	//grab the text from the EditText object
            	String  editText = deviceNameET.getText().toString();
            	if(editText.isEmpty()){//the field is empty
            		
            		//create an alert dialog
            		AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddDeviceActivity.this);
            		
            		//set the message to be displayed
                    alertDialog.setMessage(R.string.dialog_message);
                    
                    //set the behaviour of the button
                    alertDialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    //display the alert dialog
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                    return;
            	}  	
            	
                if (mDeviceId != -1) {
                    updateDevice();
                    startDeviceDetailsActivity(mDeviceId);
                } else {
                    addDevice();
                    startSingleUnitActivity();
                }
            }
        });
    }
    
    // -- Action Bar

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_device, menu);
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
            
            // make sure we pass in the hardware unit id
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra(AllUnitsActivity.EXTRA_UNIT_ID, mHardwareUnitId);
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // -- Intents
    
    public void startSingleUnitActivity() {
        Intent intent = new Intent(this, SingleUnitActivity.class);
        intent.putExtra(EXTRA_UNIT_ID, mHardwareUnitId);  // pass back hardware unit id
        startActivity(intent);
    }
    
    public void startDeviceDetailsActivity(long deviceId) {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(SingleUnitActivity.EXTRA_DEVICE_ID, deviceId);
        startActivity(intent);
    }
    
    // -- Model
    
    public void addDevice() {
        EditText et = (EditText) findViewById(R.id.deviceNameEditText);
        String deviceName = et.getText().toString();
        long deviceId = mDeviceDataSource.addDevice(mHardwareUnitId, mSocketId, deviceName, mSelectedDeviceTypeId);
    }
    
    public void updateDevice() {
        EditText et = (EditText) findViewById(R.id.deviceNameEditText);
        String deviceName = et.getText().toString();
        mDeviceDataSource.updateDevice(mDeviceId, deviceName, mSelectedDeviceTypeId);
    }
}
