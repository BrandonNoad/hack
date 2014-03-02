package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

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
        setContentView(R.layout.activity_all_units);
        
        mHardwareUnitsDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitsDataSource.open();
        
        mAllUnitsList = mHardwareUnitsDataSource.getAllHardwareUnits();
        
        Button addUnitButton = (Button) findViewById(R.id.addUnit);
        
        addUnitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                initializeBluetooth();                
            }
        });

        addUnitButton.setEnabled(false);
        
        mHardwareUnitAdapter = new HardwareUnitAdapter(this, mAllUnitsList);
        
        ListView listOfAllHardware = (ListView) findViewById(R.id.listOfAllHardware);
        listOfAllHardware.setAdapter(mHardwareUnitAdapter);
        
        listOfAllHardware.setOnItemClickListener(new OnItemClickListener() {
           public void onItemClick(AdapterView parent, View v, int position, long id) {
               HardwareUnit unit = (HardwareUnit) parent.getItemAtPosition(position);
               startSingleUnitActivity(unit.getId());
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
        getMenuInflater().inflate(R.menu.all_hardware, menu);
        return true;
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
    
    

}
