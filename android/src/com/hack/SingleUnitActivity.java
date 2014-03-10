package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.Toast;

public class SingleUnitActivity extends Activity {
  
    public final static String EXTRA_HARDWARE_UNIT_ID = "com.hack.HARDWARE_UNIT_ID";
    public final static String EXTRA_SOCKET_ID = "com.hack.SOCKET_ID";
    public final static String EXTRA_DEVICE_ID = "com.hack.DEVICE_ID";
    public final static int NUM_SOCKETS = 4;
    
    String mBasePath = "http://192.168.43.239:8080/";
    
    private HardwareUnitDataSource mHardwareUnitDataSource;
    private DeviceDataSource mDeviceDataSource;
    private ArrayList<Device> mDevices = new ArrayList<Device>();
    private DeviceAdapter mDeviceAdapter;
    private long mHardwareUnitId;
    private ActionBar mActionBar;
    private Device mSelectedDevice = null;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_unit);
        // Show the Up button in the action bar.
        setupActionBar();
        mActionBar = getActionBar();
        
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        
        Intent intent = getIntent();
        mHardwareUnitId = intent.getLongExtra(AllUnitsActivity.EXTRA_UNIT_ID, -1);
        if (mHardwareUnitId >= 0) {
            displayUnit(mHardwareUnitId);
            displayDevices(mHardwareUnitId);
           
        }
        
        GridView socketGrid = (GridView) findViewById(R.id.socketGrid);
        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        socketGrid.setAdapter(mDeviceAdapter);
        
        socketGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                if (device != null) {
                    startDeviceDetailsActivity(device.getId());
                } else {
                    startAddDeviceActivity(position);
                }
            }
         });
        
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
                    mSelectedDevice = device;
                    return true;                  
                }
            }
        });
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
        HardwareUnit unit = mHardwareUnitDataSource.getHardwareUnitById(unitId);
        mActionBar.setTitle(unit.getName());
    }
    
    public void displayDevices(long unitId) {
        for (int i = 0; i < NUM_SOCKETS; i++) {
            Device d = mDeviceDataSource.getDevice(unitId, i);
            mDevices.add(d);
        }
    }
    
    public void deleteDevice(Device device) {
        mDeviceDataSource.deleteDeviceById(device.getId());
        mDevices.set((int) device.getSocketId(), null);
        mDeviceAdapter.notifyDataSetChanged();
    }
    
    public void startDeviceDetailsActivity(long deviceId) {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        startActivity(intent);
    }
    
    public void startAddDeviceActivity(long socketId) {
        Intent intent = new Intent(this, AddDeviceActivity.class);
        intent.putExtra(EXTRA_HARDWARE_UNIT_ID, mHardwareUnitId);
        intent.putExtra(EXTRA_SOCKET_ID, socketId);  
        startActivity(intent);
    }
    
    ActionMode mActionMode = null;
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
                    deleteDevice(mSelectedDevice);
                    mSelectedDevice = null; 
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
}
