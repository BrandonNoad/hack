package com.hack;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

public class SingleUnitActivity extends Activity {

    // -- Constants

    public final static String EXTRA_HARDWARE_UNIT_ID = "com.hack.HARDWARE_UNIT_ID";
    public final static String EXTRA_SOCKET_ID = "com.hack.SOCKET_ID";
    public final static String EXTRA_DEVICE_ID = "com.hack.DEVICE_ID";
    public final static String EXTRA_TITLE = "com.hack.TITLE";
    public final static int NUM_SOCKETS = 4;

    // -- Member Variables

    private HardwareUnitDataSource mHardwareUnitDataSource;
    private DeviceDataSource mDeviceDataSource;
    private ArrayList<Device> mDevices = new ArrayList<Device>();
    private DeviceAdapter mDeviceAdapter;
    private long mHardwareUnitId;
    private ActionBar mActionBar;
    private Device mSelectedDevice = null;
    private ActionMode mActionMode = null;
    private HackConnectionManager mConnectionManager;
    private HardwareUnit mHardwareUnit;

    // -- Initialize Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_unit);

        // Show the Up button in the action bar.
        setupActionBar();

        // initialize members
        mActionBar = getActionBar();        
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();

        mConnectionManager = ((HackApplication) getApplicationContext()).getConnectionManager();

        // get hardware unit id from previous activity
        Intent intent = getIntent();
        mHardwareUnitId = intent.getLongExtra(AllUnitsActivity.EXTRA_UNIT_ID, -1);
     

        GridView socketGrid = (GridView) findViewById(R.id.socketGrid);
        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        socketGrid.setAdapter(mDeviceAdapter);

        // set up event listeners

        // regular click
        socketGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                if (mActionMode != null) {
                    return;  // prevent regular click when action mode is enabled
                } else if (device != null) {
                    startDeviceDetailsActivity(device.getId());
                } else {
                    startAddDeviceActivity(position);
                }
            }
        });

        // long click
        socketGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            // Called when the user long-clicks on someView
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                if (device == null || mActionMode != null) {                       
                    return false;
                } else {                
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = SingleUnitActivity.this.startActionMode(mActionModeCallback);
                    v.setSelected(true);
                    mDeviceAdapter.setSelectedIndex(position);  // set background colour
                    mSelectedDevice = device;
                    return true;                  
                }
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        if (mHardwareUnitId >= 0) {
            // display unit/devices
            displayUnit(mHardwareUnitId);
            displayDevices(mHardwareUnitId);
        }
    }

    public void displayUnit(long unitId) {
        mHardwareUnit = mHardwareUnitDataSource.getHardwareUnitById(unitId);
        mActionBar.setTitle(mHardwareUnit.getName());
    }

    public void displayDevices(long unitId) {
        mDeviceAdapter.clear();
        for (int i = 0; i < NUM_SOCKETS; i++) {
            Device d = mDeviceDataSource.getDevice(unitId, i);
            mDeviceAdapter.add(d);
        }
        mDeviceAdapter.notifyDataSetChanged();
        
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
        case R.id.action_refresh:
            String url = "http://" + mHardwareUnit.getBasePath() + ":" + mHardwareUnit.getPortNumber() + "/hack/refresh";
            HackCommand refreshCommand = new HackCommand(SingleUnitActivity.this, mHardwareUnit, url) {

                @Override
                public void doSuccess(JSONObject response) {
                    super.doSuccess(response);
                    
                    JSONObject data = new JSONObject();
                    try {
                        data = response.getJSONObject("data");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    parseJSONResponse(data);
                    displayDevices(getHardwareUnit().getId());
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
            inflater.inflate(R.menu.single_unit_action_mode, menu);
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
                deleteDevice(mSelectedDevice);                    
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
            mDeviceAdapter.setSelectedIndex(-1);  // set background color back to transparent
        }
    };

    // -- Intents

    public void startAddDeviceActivity(long socketId) {
        Intent intent = new Intent(this, AddDeviceActivity.class);        
        intent.putExtra(EXTRA_HARDWARE_UNIT_ID, mHardwareUnitId);
        intent.putExtra(EXTRA_SOCKET_ID, socketId);
        intent.putExtra(EXTRA_TITLE, "Add New Device");
        startActivity(intent);
    }

    public void startDeviceDetailsActivity(long deviceId) {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        intent.putExtra(EXTRA_HARDWARE_UNIT_ID, mHardwareUnitId);
        startActivity(intent);
    }

    // -- Model

    public void deleteDevice(Device device) {
        String url = "http://" + mHardwareUnit.getBasePath() + ":" + mHardwareUnit.getPortNumber() + "/hack/delete?socket=" + device.getSocketId();
        HackCommand deleteCommand = new HackCommand(SingleUnitActivity.this, mHardwareUnit, url) {

            @Override
            public void doSuccess(JSONObject response) {
                super.doSuccess(response);
                mDeviceDataSource.deleteDeviceById(mSelectedDevice.getId());
                mDevices.set((int) mSelectedDevice.getSocketId(), null);
                mDeviceAdapter.notifyDataSetChanged();
                mSelectedDevice = null;
            }

        };
        
        deleteCommand.send();


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
                            ", current espruino time (seconds): " + espruinoCurrentTime + 
                            ", total time on: " + totalTimeOn +
                            ", on since time (espruino seconds): " + onSinceTime);                                
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

}
