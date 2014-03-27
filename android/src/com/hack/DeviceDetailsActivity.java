package com.hack;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
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

    // -- Member Variables 

    private long mDeviceId;
    private Device mDevice;
    private DeviceDataSource mDeviceDataSource;
    private long mSocketId;
    private int mDeviceState;

    private Button mEnableTimerButton;
    private TextView mDeviceTimerDetails;
    private Switch mDeviceStateSwitch;
    private TextView mDeviceType;
    private TextView mDeviceOnSinceTime;
    private TextView mDeviceTotalTimeOn;
    private TextView mDevicePowerUsage;

    private HardwareUnitDataSource mHardwareUnitDataSource;

    private Timer mTimer;
    private TimerDataSource mTimerDataSource;   

    private ActionBar mActionBar;
    private ActionMode mActionMode = null;

    private long mHardwareUnitId;
    private HardwareUnit mHardwareUnit;

    private boolean isCheckedChangeEnabled = true;


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

        // get device id from previous activity
        Intent intent = getIntent();
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        mHardwareUnitId = intent.getLongExtra(SingleUnitActivity.EXTRA_HARDWARE_UNIT_ID, -1);
        
        mHardwareUnit = mHardwareUnitDataSource.getHardwareUnitById(mHardwareUnitId);

        mEnableTimerButton = (Button) findViewById(R.id.enableTimerButton);
        mDeviceTimerDetails = (TextView) findViewById(R.id.deviceTimerDetails);
        mDeviceStateSwitch = (Switch) findViewById(R.id.deviceStateSwitch);
        mDeviceType = (TextView) findViewById(R.id.deviceType);
        mDeviceOnSinceTime = (TextView) findViewById(R.id.deviceLastTimeOn);
        mDeviceTotalTimeOn = (TextView) findViewById(R.id.deviceTotalTimeOn);
        mDevicePowerUsage = (TextView) findViewById(R.id.devicePowerUsage);
        
        // update on-screen stats        
        updateUIDeviceStatistics();

        // set up event listeners
        mDeviceStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isCheckedChangeEnabled) {
                    String url = "http://" + mHardwareUnit.getBasePath() + ":" + mHardwareUnit.getPortNumber() + "/hack";
                    if (isChecked) {
                        url += "/on?socket=" + mSocketId;
                    } else {
                        url += "/off?socket=" + mSocketId;
                    }
                    Log.i("DeviceDetailsActivity - onCheckChanged()", "Requested url: " + url);

                    HackCommand toggleCommand = new HackCommand(DeviceDetailsActivity.this, mHardwareUnit, url) {

                        @Override
                        public void doSuccess(JSONObject data, String message) {
                            super.doSuccess(data, message);
                            parseJSONResponse(data);
                            updateUIDeviceStatistics();
                            Log.i("DeviceDetailsActivity - onPostExecute","device statistics updated");
                        }

                        @Override
                        public void doFail(String message) {
                            super.doFail(message);
                            // change switch back to its old position
                            // temporarily disable event listener so we can switch it back without firing event
                            isCheckedChangeEnabled = false;
                            mDeviceStateSwitch.toggle();
                            isCheckedChangeEnabled = true;
                        }

                    };

                    // send command
                    toggleCommand.send();
                }
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
        case R.id.action_edit:
            startEditDeviceActivity();
            break;
        case R.id.action_refresh:
            String url = "http://" + mHardwareUnit.getBasePath() + ":" + mHardwareUnit.getPortNumber() + "/hack/refresh";
            HackCommand refreshCommand = new HackCommand(DeviceDetailsActivity.this, mHardwareUnit, url) {

                @Override
                public void doSuccess(JSONObject data, String message) {
                    super.doSuccess(data, message);
                    parseJSONResponse(data);
                    updateUIDeviceStatistics();
                    Log.i("DeviceDetailsActivity - onPostExecute","device statistics updated");
                }
            };

            // send command
            refreshCommand.send();

            break;
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
            inflater.inflate(R.menu.all_units_action_mode, menu);
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

    public void startEditDeviceActivity() {
        if (mDevice != null) {
            Intent intent = new Intent(this, AddDeviceActivity.class);
            intent.putExtra(SingleUnitActivity.EXTRA_DEVICE_ID, mDeviceId);
            intent.putExtra(SingleUnitActivity.EXTRA_HARDWARE_UNIT_ID, mDevice.getHardwareUnitId());
            intent.putExtra(SingleUnitActivity.EXTRA_SOCKET_ID, mDevice.getSocketId());
            intent.putExtra(SingleUnitActivity.EXTRA_TITLE, "Edit Device");
            startActivity(intent);
        }
    }

    // -- Misc

    // kWh
    public long calculatePowerUsage(long totalTimeOn, long deviceTypeId) {
        int multFactor = 1;
        switch ((int) deviceTypeId) {
        case 1: // simple
            multFactor = 200;
            break;
        case 2: 
            multFactor = 60;
            break;
        case 3:
            multFactor = 1000;
            break;
        default:
            break;
        }

        return ((totalTimeOn / 360) * multFactor) / 1000; 
    }

    private void deleteTimer() {
        if (mTimer != null && mDevice != null) {
            // delete timer from db
            mTimerDataSource.deleteTimerByDeviceId(mDevice.getId());
            mTimer = null;

            // show enable button
            mDeviceTimerDetails.setVisibility(View.GONE);
            mEnableTimerButton.setVisibility(View.VISIBLE);

        }
    }
    
    private void parseJSONResponse(JSONObject data) {
        if (data != null) {
            ArrayList<Device> devices = mDeviceDataSource.getAllDevicesForHardwareUnit(mHardwareUnitId);
            // update each of the devices in the db
            for (Device d : devices) {
                Log.i("DeviceDetailsActivity - parseJSONResponse()", "updating device at socket: " + d.getSocketId());
                int newState = 0;
                long totalTimeOn = 0; // milliseconds
                long onSinceTime = -1; // milliseconds
                long espruinoCurrentTime = 0; // milliseconds
                try {
                    espruinoCurrentTime = data.getLong("currentTime");
                    JSONObject outlet = data.getJSONArray("outlets").getJSONObject((int) d.getSocketId());
                    newState = outlet.getInt("state");                                    
                    totalTimeOn = outlet.getLong("totalTimeOn");
                    onSinceTime = outlet.getLong("onSinceTime");
                    Log.i("DeviceDetailsActivity - parseJSONResponse", 
                            "new state: " + newState + 
                            ", current time: " + espruinoCurrentTime + 
                            ", total time on: " + totalTimeOn +
                            ", on since time: " + onSinceTime);                                
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // update db
                // time conversion
                long now = new Date().getTime();
                if (onSinceTime != -1) {
                    onSinceTime = now - ((espruinoCurrentTime - onSinceTime) * 1000);
                }                               
                mDeviceDataSource.updateDevice(d.getId(), newState, totalTimeOn, onSinceTime);
            }
        }
    }

    private void updateUIDeviceStatistics() {
        if (mDeviceDataSource != null) {
            mDevice = mDeviceDataSource.getDeviceById(mDeviceId);
            if (mDevice != null) {
                mSocketId = mDevice.getSocketId();
                // title
                mActionBar.setTitle(mDevice.getName());
                // state
                mDeviceState = mDevice.getState();
                isCheckedChangeEnabled = false;
                if (mDeviceState == 1) {  // 1 is off
                    mDeviceStateSwitch.setChecked(false);
                } else {
                    mDeviceStateSwitch.setChecked(true);
                }
                isCheckedChangeEnabled = true;
                // type
                mDeviceType.setText(getString(R.string.type_colon) + " " + mDevice.getType());
                // on since time
                String onSinceTime = "OFF";
                Log.i("DeviceDetailsActivity - updateUIDeviceStatistics()", "Actual on Since: " + mDevice.getOnSinceTime());
                if (mDevice.getOnSinceTime() != -1) {
                    Date onSinceDate = new Date(mDevice.getOnSinceTime());
                    DateFormat df = DateFormat.getTimeInstance();
                    onSinceTime = df.format(onSinceDate);
                }
                mDeviceOnSinceTime.setText(getString(R.string.on_since_colon) + " " +  onSinceTime);
                // total time on and power usage
                long totalTimeOn = mDevice.getTotalTimeOn();
                long powerUsage = calculatePowerUsage(totalTimeOn, mDevice.getTypeId());
                mDeviceTotalTimeOn.setText(getString(R.string.total_time_on_colon) + " " + (totalTimeOn / 60) + " mins");
                mDevicePowerUsage.setText(getString(R.string.power_usage_colon) + " " + powerUsage + " kWh");
            }
        }
        
        // get timer info
        if (mTimerDataSource != null) {
            mTimer = mTimerDataSource.getTimerByDeviceId(mDeviceId);
            // no timer set?
            if (mTimer == null) {
                mDeviceTimerDetails.setVisibility(View.GONE);
                mEnableTimerButton.setVisibility(View.VISIBLE);
            } else {
                mEnableTimerButton.setVisibility(View.GONE);
                mDeviceTimerDetails.setVisibility(View.VISIBLE);
                mDeviceTimerDetails.setText("Turn ON between: " + mTimer.getTimeOn() + " - " + mTimer.getTimeOff());
            }
        }
    }

}
