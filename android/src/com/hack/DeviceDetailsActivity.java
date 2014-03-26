package com.hack;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class DeviceDetailsActivity extends Activity {
    
    // -- Constants

    
    // -- Member Variables 
    
    private long mDeviceId;
    private Device mDevice;
    private DeviceDataSource mDeviceDataSource;
    private long mSocketId;
    private int mDeviceState;
    
    private Button mEnableTimerButton;
    private TextView mDeviceTimerDetails;
    
    private HardwareUnitDataSource mHardwareUnitDataSource;
    
    private Timer mTimer;
    private TimerDataSource mTimerDataSource;   
    
    private ActionBar mActionBar;
    private ActionMode mActionMode = null;
    
    private ProgressDialog mProgressDialog;
    
    // -- Initialize Activity
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);
        // Show the Up button in the action bar.
        setupActionBar();
        mActionBar = getActionBar();
                
        // initialize members
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        mTimerDataSource = new TimerDataSource(this);
        mTimerDataSource.open();
        
//        mHttpConnectionManager = new HackWifiAdapter(this, this);
        
        // get device id from previous activity
        Intent intent = getIntent();
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        
        mEnableTimerButton = (Button) findViewById(R.id.enableTimerButton);
        mDeviceTimerDetails = (TextView) findViewById(R.id.deviceTimerDetails);
        Switch deviceStateSwitch = (Switch) findViewById(R.id.deviceStateSwitch);
        TextView deviceType = (TextView) findViewById(R.id.deviceType);
        TextView deviceLastTimeOn = (TextView) findViewById(R.id.deviceLastTimeOn);
        TextView deviceTotalTimeOn = (TextView) findViewById(R.id.deviceTotalTimeOn);
        TextView devicePowerUsage = (TextView) findViewById(R.id.devicePowerUsage);
        
        // TODO: update device state from server response
        mDevice = mDeviceDataSource.getDeviceById(mDeviceId);
        if (mDevice != null) {
            mActionBar.setTitle(mDevice.getName());
            mDeviceState = mDevice.getState();
            if (mDeviceState == 0) {
                deviceStateSwitch.setChecked(false);
            } else {
                deviceStateSwitch.setChecked(true);
            }
            mSocketId = mDevice.getSocketId();
            
            deviceType.setText(getString(R.string.type_colon) + " " + mDevice.getType());
            deviceLastTimeOn.setText(getString(R.string.last_time_on_colon) + " " + "TODO");
            
            int totalTimeOn = mDevice.getTotalTimeOn();
            int powerUsage = calculatePowerUsage(totalTimeOn);
            deviceTotalTimeOn.setText(getString(R.string.total_time_on_colon) + " " + totalTimeOn);
            devicePowerUsage.setText(getString(R.string.power_usage_colon) + " " + powerUsage);
            
            // get timer info
            mTimer = mTimerDataSource.getTimerByDeviceId(mDeviceId);
            
            if (mTimer == null) {
                mDeviceTimerDetails.setVisibility(View.GONE);
                mEnableTimerButton.setVisibility(View.VISIBLE);
            } else {
                mEnableTimerButton.setVisibility(View.GONE);
                mDeviceTimerDetails.setVisibility(View.VISIBLE);
                mDeviceTimerDetails.setText("Turn ON between: " + mTimer.getTimeOn() + " - " + mTimer.getTimeOff());
            }
        }
        
        // set up event listeners
        deviceStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // disable switch
                buttonView.setEnabled(false);
                String url = "/hack";
                if (isChecked) {
                    url += "/on?socket=" + mSocketId;
                } else {
                    url += "/off?socket=" + mSocketId;
                }
                Log.i("DeviceDetailsActivity - onCheckChanged()", "Requested url: " + url);
                
                HardwareUnit unit = mHardwareUnitDataSource.getHardwareUnitById(mDevice.getHardwareUnitId());
                HackConnectionManager connMgr = ((HackApplication) getApplicationContext()).getConnectionManager();                
                connMgr.submitRequest(new HackCommand(unit, url, getApplicationContext()) {
                    
                    @Override
                    public void onResponseReceived(JSONObject json) {
                        // TODO Auto-generated method stub
                    }
                    
                });
            }
        });
        
      mEnableTimerButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
              startSetTimerActivity();
          }
      });
      
      mDeviceTimerDetails.setOnLongClickListener(new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            
            if (mActionMode != null) {
                return false;
            } else {                
                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = DeviceDetailsActivity.this.startActionMode(mActionModeCallback);
                return true;
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
        getMenuInflater().inflate(R.menu.device_details, menu);
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
            upIntent.putExtra(AllUnitsActivity.EXTRA_UNIT_ID, mDevice.getHardwareUnitId());            
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
// -- Action Mode    
    
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteTimer();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
    
    // -- Intents
    
    public void startSetTimerActivity() {
        Intent intent = new Intent(this, SetTimerActivity.class);
        intent.putExtra(SingleUnitActivity.EXTRA_DEVICE_ID, mDeviceId);
        startActivity(intent);
    }
  
    // -- Misc
    
    // TODO: derive this formula
    public int calculatePowerUsage(int totalTimeOn) {
        return totalTimeOn * 5;
    }
    
    public void deleteTimer() {
        if (mTimer != null && mDevice != null) {
            // delete timer from db
            mTimerDataSource.deleteTimerByDeviceId(mDevice.getId());
            mTimer = null;
            
            // show enable button
            mDeviceTimerDetails.setVisibility(View.GONE);
            mEnableTimerButton.setVisibility(View.VISIBLE);
            
        }
    }
    
}
