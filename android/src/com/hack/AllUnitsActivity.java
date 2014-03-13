package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This Activity lists all the HACK hardware units and allows the user to add 
 * new hardware units using Bluetooth via a button in the action bar.
 */
public class AllUnitsActivity extends Activity {
    
    // -- Intent extras
    public static final String EXTRA_UNIT_ID = "com.hack.UNIT_ID";
    public static final String EXTRA_BT_DEVICE = "com.hack.BT_DEVICE";
    public static final int REQUEST_ENABLE_BT = 1;
    
    // -- Espruino UUID and MAC address
  
    private final String ESPRUINO_MAC = "20:13:11:19:00:76";
    
    // -- Bluetooth member variables
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;    
   
    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mSelectedBluetoothDevice;
    
    //  -- List of Hardware Unit member variables
    private HardwareUnitAdapter mHardwareUnitAdapter;
    private ArrayList<HardwareUnit> mAllUnitsList = new ArrayList<HardwareUnit>(); 
    private HardwareUnitDataSource mHardwareUnitDataSource;
    private HardwareUnit mSelectedHardwareUnit;
    
    // onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        // rename action bar title
        getActionBar().setTitle(R.string.title_hardware_units);
        
        setContentView(R.layout.activity_all_units);     
        
        // initialize members
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        mAllUnitsList = mHardwareUnitDataSource.getAllHardwareUnits();  // get hardware units from db
        mHardwareUnitAdapter = new HardwareUnitAdapter(this, mAllUnitsList);        
        ListView listOfAllHardware = (ListView) findViewById(R.id.listOfAllHardware);
        listOfAllHardware.setAdapter(mHardwareUnitAdapter);
        
        // -- Event listeners
        
        // regular click
        listOfAllHardware.setOnItemClickListener(new OnItemClickListener() {
           public void onItemClick(AdapterView parent, View v, int position, long id) {
               if (mActionMode != null) {
                   return;
               }
               HardwareUnit unit = (HardwareUnit) parent.getItemAtPosition(position);
               startSingleUnitActivity(unit.getId());
           }
        });
        
        // long click
        listOfAllHardware.setOnItemLongClickListener(new OnItemLongClickListener() {
            // Called when the user long-clicks on someView
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                HardwareUnit hu = (HardwareUnit) parent.getItemAtPosition(position);
                if (hu == null || mActionMode != null) {
                    return false;
                } else {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = AllUnitsActivity.this.startActionMode(mActionModeCallback);
                    v.setSelected(true);
                    mHardwareUnitAdapter.setSelectedIndex(position);  // set background colour
                    mSelectedHardwareUnit = hu;
                    return true;
                }
            }
        }); 
    }  // onCreate()
    
    // -- Bluetooth
    
    private void initializeBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // no Bluetooth
        if (mBluetoothAdapter == null) {
          Log.i("AllUnitsActivity - turnOnBluetooth()", "device does not support blueooth");
          finish();
          return;
        }
        // Bluetooth already enabled
        if (mBluetoothAdapter.isEnabled()) {
            getPairedBTDevices();            
        } else {  // Bluetooth not enabled            
            // Ask user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    
    /**
     * Callback for startActivityResult() 
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            // Enable Bluetooth?
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("AllUnitsActivity - onActivityResult()", "Bluetooth successfully enabled");
                    getPairedBTDevices();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i("AllUnitsActivity - onActivityResult()", "Bluetooth not enabled");
                }
                break;
            default:
                break;
        }
    }
    
    public void getPairedBTDevices() {
        mBluetoothDevices = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());
        int deviceCount = mBluetoothDevices.size();
        Log.i("AllUnitsActivity - getPairedBTDevices", "# devices: " + deviceCount);
        String[] bluetoothDeviceNames = new String[deviceCount];
        if (deviceCount > 0) {
            int i = 0;
            for (BluetoothDevice device : mBluetoothDevices) {
                bluetoothDeviceNames[i] = mBluetoothDevices.get(i).getName();
                i++;                
            }
        }
        // show bluetooth dialog
        DialogFragment bluetoothDialogFragment = BluetoothDialogFragment.newInstance(bluetoothDeviceNames);
        bluetoothDialogFragment.show(getFragmentManager(), "bluetooth_dialog");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.all_units, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new:
                mSelectedBluetoothDevice = null;
                initializeBluetooth();
                return true;            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void addHardwareUnit() {
        Intent intent = new Intent(this, AddDeviceActivity.class);
        startActivity(intent);
    }
    
    public void deleteHardwareUnit(HardwareUnit hu) {
        int result = mHardwareUnitDataSource.deleteHardwareUnitById(hu.getId());
        Log.i("AllUnitsActivity - deleteHardwareUnit()", "deleted?: " + result);
        mHardwareUnitAdapter.remove(hu);
    }
    
    public void startSingleUnitActivity(long unitId) {
        Intent intent = new Intent(this, SingleUnitActivity.class);
        intent.putExtra(EXTRA_UNIT_ID, unitId);
        startActivity(intent);
    } 
    
    public void startAddHardwareUnitActivity() {
        Intent intent = new Intent(this, AddHardwareUnitActivity.class);
        intent.putExtra(EXTRA_BT_DEVICE, mSelectedBluetoothDevice);
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
                    deleteHardwareUnit(mSelectedHardwareUnit);
                    mSelectedHardwareUnit = null;     
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mHardwareUnitAdapter.setSelectedIndex(-1);  // reset background colour
        }
    };
      
    public void doBluetoothDialogOkClick() {
        if (mSelectedBluetoothDevice != null && mSelectedBluetoothDevice.getAddress().equals(ESPRUINO_MAC)) {
            Toast.makeText(getApplicationContext(), "Espruino!", 
            Toast.LENGTH_LONG).show();
            
            startAddHardwareUnitActivity();
            
        } else {
            Toast.makeText(getApplicationContext(), "Not Espruino", 
            Toast.LENGTH_LONG).show();
            
        }
    }   
    
    public void setBtDevice(int position) {
        mSelectedBluetoothDevice = mBluetoothDevices.get(position);
    }
    
    public static class BluetoothDialogFragment extends DialogFragment { 
        
        public static BluetoothDialogFragment newInstance(String[] btDeviceNames) {
            BluetoothDialogFragment btFrag = new BluetoothDialogFragment();
            btFrag.setCancelable(false);  // prevents dismiss on press outside dialog
            Bundle args = new Bundle();
            args.putStringArray("btDeviceNames", btDeviceNames);
            btFrag.setArguments(args);         
            return btFrag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            
            String[] btDeviceNames = getArguments().getStringArray("btDeviceNames");
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            
            builder.setTitle(R.string.title_select_hardware_unit);
            
            builder.setPositiveButton(R.string.ok,  new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {//                   
                    ((AllUnitsActivity) getActivity()).doBluetoothDialogOkClick();
                }
            });
            
            builder.setNegativeButton(R.string.cancel,  new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((AllUnitsActivity) getActivity()).mSelectedBluetoothDevice = null;
                }
            });
            
            builder.setSingleChoiceItems(btDeviceNames, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ((AllUnitsActivity) getActivity()).setBtDevice(which);
                }
            });

            return builder.create();
        }
    }
    
    
    

}
