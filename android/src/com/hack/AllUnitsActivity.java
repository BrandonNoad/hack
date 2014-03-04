package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class AllUnitsActivity extends Activity {
    
    public final static String EXTRA_UNIT_ID = "com.hack.UNIT_ID";
    public static final int REQUEST_ENABLE_BT = 1;
    public static final UUID ESPRUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final String ESPRUINO_MAC = "20:13:11:19:00:76";
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;    
    private ConnectedThread mConnectedThread;
    
    private HardwareUnitAdapter mHardwareUnitAdapter;
    private ArrayList<HardwareUnit> mAllUnitsList = new ArrayList<HardwareUnit>(); 
    private HardwareUnitDataSource mHardwareUnitsDataSource;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_hardware_units);
        setContentView(R.layout.activity_all_units);     
        
        mHardwareUnitsDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitsDataSource.open();
        
        mAllUnitsList = mHardwareUnitsDataSource.getAllHardwareUnits();
        
      
        
//        addUnitButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                initializeBluetooth();                
//            }
//        });
//
//        addUnitButton.setEnabled(false);
        
        mHardwareUnitAdapter = new HardwareUnitAdapter(this, mAllUnitsList);
        
        ListView listOfAllHardware = (ListView) findViewById(R.id.listOfAllHardware);
        listOfAllHardware.setAdapter(mHardwareUnitAdapter);
        
        listOfAllHardware.setOnItemClickListener(new OnItemClickListener() {
           public void onItemClick(AdapterView parent, View v, int position, long id) {
               HardwareUnit unit = (HardwareUnit) parent.getItemAtPosition(position);
               startSingleUnitActivity(unit.getId());
           }
        });
        
        listOfAllHardware.setOnItemLongClickListener(new OnItemLongClickListener() {
            // Called when the user long-clicks on someView
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getApplicationContext(), "Long Click", 
                        Toast.LENGTH_LONG).show();         
                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = AllUnitsActivity.this.startActionMode(mActionModeCallback);
                v.setSelected(true);
                return true;
            }
        });
        
        
        
    }
    
    private void initializeBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        
        if (mBluetoothAdapter.isEnabled()) {
            connectToEspruino(ESPRUINO_MAC, ESPRUINO_UUID);
        } else {
            
            // Ask user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    
    public void connectToEspruino(String macAddress, UUID uuid) {
        BluetoothDevice remoteBtDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
        ConnectThread ct = new ConnectThread(remoteBtDevice, uuid);
        ct.start();             
        Log.i("Main Activity", "Waiting for ConnectThread");        
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
                Toast.makeText(getApplicationContext(), "New", 
                        Toast.LENGTH_LONG).show();                
                
                return true;            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void addHardwareUnit() {
        Intent intent = new Intent(this, AddDeviceActivity.class);
        startActivity(intent);
    }
    
    public void startSingleUnitActivity(long unitId) {
        Intent intent = new Intent(this, SingleUnitActivity.class);
        intent.putExtra(EXTRA_UNIT_ID, unitId);
        startActivity(intent);
    }   
      
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch(requestCode) {            
            // Enable Bluetooth?
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("Main Activity", "Bluetooth successfully enabled");
//                    queryDevices();
                    connectToEspruino(ESPRUINO_MAC, ESPRUINO_UUID);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i("Main Activity", "Bluetooth not enabled");
                }
                break;
            default:
                break;
        }
    } 
    
    private synchronized void manageConnectedSocket(BluetoothSocket btSocket) {
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();       
    }    
    
    public class ConnectThread extends Thread {
        
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
         
        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.i("Connect Thread", "ctor starting");
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
     
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {                
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) { }
            mmSocket = tmp;
        }
     
        public void run() {
            Log.i("ConnectThread", "Running new thread");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.i("ConnectThread", "Unable to connect.");
                try {
                    mmSocket.close();
                } catch (IOException closeException) { 
                    // do something
                }
                
                return;
            }
            Log.i("Connect Thread", "Connection successful");
            
            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }
     
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { 
                // do something
            }
        }
    }
    
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
     
        public ConnectedThread(BluetoothSocket socket) {
            Log.i("Connected Thread", "ctor starting");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
     
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
     
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            
            Log.i("Connected Thread", "Ready to write bytes");
        }
     
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
            
        }
     
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.i("ConnectedThread", "Bytes sucessfully written.");
            } catch (IOException e) { }
            
        }
     
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
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
                    Toast.makeText(getApplicationContext(), "Delete", 
                            Toast.LENGTH_LONG).show();         
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
        }
    };
    
    
    
    

}
