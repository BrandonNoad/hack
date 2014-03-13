package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.hack.AllUnitsActivity.BluetoothDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddHardwareUnitActivity extends Activity {
    
    private final UUID ESPRUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    
    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mBluetoothDevice;
    private Button mAddHardwareUnitButton;
    
    private HardwareUnitDataSource mHardwareUnitDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hardware_unit);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        
        mAddHardwareUnitButton = (Button) findViewById(R.id.add_hardware_unit_button);
        
        mAddHardwareUnitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addHardwareUnit();
            }
        });
        
        // disable button until bluetooth connection is established
        mAddHardwareUnitButton.setEnabled(false);
        
        Bundle bundle = getIntent().getExtras();
        mBluetoothDevice = bundle.getParcelable(AllUnitsActivity.EXTRA_BT_DEVICE);
        
        // start bluetooth connection
        startBTConnection();
        
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
        getMenuInflater().inflate(R.menu.add_hardware_unit, menu);
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
    
    public void addHardwareUnit() {
        EditText nameET = (EditText) findViewById(R.id.editTextHardwareUnitName);
        EditText accessPointNameET = (EditText) findViewById(R.id.editTextAccessPointName);
        EditText wpa2KeyET = (EditText) findViewById(R.id.editTextWpa2Key);
        EditText basePathET = (EditText) findViewById(R.id.editTextBasePath);
        EditText portNumberET = (EditText) findViewById(R.id.editTextPortNumber);
        
        // add new unit to db
        long huId = mHardwareUnitDataSource.addHardwareUnit(nameET.getText().toString(),
                                                basePathET.getText().toString(),
                                                Integer.parseInt(portNumberET.getText().toString()));
        Log.i("AddHardwareUnitActivity - addHardwareUnit()", "unit Id: " + huId);
        
        // send details to espruino via BT
        String accessPointName = accessPointNameET.getText().toString();
        String wpa2Key = wpa2KeyET.getText().toString();
        String portNumber = portNumberET.getText().toString();
        Log.i("AddHardwareUnitActivity - addHardwareUnit()", "Access Point: " + accessPointName + ", WPA2 Key: " + wpa2Key + ", Port Number: " + portNumber);
        
        if (mConnectedThread != null) {
            mConnectedThread.write(" ".getBytes());
            mConnectedThread.write("set_ap".getBytes());
            mConnectedThread.write((accessPointName + "|").getBytes());
            mConnectedThread.write("set_wpa".getBytes());
            mConnectedThread.write((wpa2Key + "|").getBytes());
            mConnectedThread.write("set_port".getBytes());
            mConnectedThread.write((portNumber + "|").getBytes());
            mConnectedThread.write("save".getBytes());
        } else {
            Log.i("Connected Thread", "mConnectedThread is null");
        }
               
        // close BT connection
        mConnectedThread.cancel();
        mConnectThread.cancel();
        
        // return to all units activity
        startAllUnitsActivity();
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
                DialogFragment bluetoothConnectionErrorDialog = BluetoothConnectionErrorDialog.newInstance();
                bluetoothConnectionErrorDialog.show(getFragmentManager(), "bluetooth_dialog");
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
    
    private synchronized void manageConnectedSocket(BluetoothSocket btSocket) {
        mAddHardwareUnitButton.setEnabled(true);
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();       
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
    
    public void startBTConnection() {
        if (mBluetoothDevice != null) {
            mConnectThread = new ConnectThread(mBluetoothDevice, this.ESPRUINO_UUID);
            mConnectThread.start();             
            Log.i("AllUnitsActivity - doClick()", "Waiting for ConnectThread");
        }
        
    }
    
    public void startAllUnitsActivity() {
        // return to All Units Activity
        Intent intent = new Intent(this, AllUnitsActivity.class);
        startActivity(intent);
    }
    
    public static class BluetoothConnectionErrorDialog extends DialogFragment { 
        
        public static BluetoothConnectionErrorDialog newInstance() {
            BluetoothConnectionErrorDialog btFrag = new BluetoothConnectionErrorDialog();
            btFrag.setCancelable(false);  // prevents dismiss on press outside dialog
            return btFrag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            
            builder.setTitle(R.string.title_bt_connection_error);
            
            builder.setMessage(R.string.would_you_like_to_try_again);
            
            builder.setPositiveButton(R.string.yes,  new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {//                   
                    ((AddHardwareUnitActivity) getActivity()).startBTConnection();
                }
            });
            
            builder.setNegativeButton(R.string.no,  new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((AddHardwareUnitActivity) getActivity()).startAllUnitsActivity();
                }
            });

            return builder.create();
        }
    }

}
