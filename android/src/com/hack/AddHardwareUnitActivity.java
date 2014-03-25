package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * This activity establishes a Bluetooth connection with a HACK hardware unit
 * and displays a form allowing the user to add a new hardware unit. The form
 * details are sent to the HACK hardware unit via Bluetooth, which initializes
 * the HACK harware unit web server.
 */
public class AddHardwareUnitActivity extends Activity {
    
    // -- Constants
    
    private static final UUID ESPRUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    
    // -- Member Variables
    
    private ConnectToBluetoothTask mConnectTask;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mBluetoothDevice;
    private Button mAddHardwareUnitButton;
    private HardwareUnitDataSource mHardwareUnitDataSource;
    
    // -- Initialize Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hardware_unit);

        setupActionBar();  // show "up" button in action bar
        
        // initialize members
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();        
        mAddHardwareUnitButton = (Button) findViewById(R.id.add_hardware_unit_button);
        
        // get BluetoothDevice passed in from previoius activity
        Bundle bundle = getIntent().getExtras();
        mBluetoothDevice = bundle.getParcelable(AllUnitsActivity.EXTRA_BT_DEVICE);
        
        // set up event listeners
        mAddHardwareUnitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addHardwareUnit();
            }
        });
        
        // disable "Add" button until bluetooth connection is established
        mAddHardwareUnitButton.setEnabled(false);
        
        // start bluetooth connection
        startBTConnection();
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
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
            }
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // -- Intents
    
    public void startAllUnitsActivity() {
        // return to All Units Activity
        Intent intent = new Intent(this, AllUnitsActivity.class);
        startActivity(intent);
    }
    
    // -- Misc.
    
    // TODO: Find a better way to close the bluetooth connection when back button is pressed
    @Override
    public void onBackPressed()
    {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        super.onBackPressed();
    }
    
    // -- Bluetooth
    
    public void startBTConnection() {
        if (mBluetoothDevice != null) {
            mConnectTask = new ConnectToBluetoothTask();
            mConnectTask.execute(mBluetoothDevice);
            Log.i("AllUnitsActivity - doClick()", "Waiting for ConnectThread");
        }
    }
    
    public void addHardwareUnit() {
        EditText nameET = (EditText) findViewById(R.id.editTextHardwareUnitName);
        EditText accessPointNameET = (EditText) findViewById(R.id.editTextAccessPointName);
        EditText wpa2KeyET = (EditText) findViewById(R.id.editTextWpa2Key);
        EditText basePathET = (EditText) findViewById(R.id.editTextBasePath);
        EditText portNumberET = (EditText) findViewById(R.id.editTextPortNumber);
        
        /********************************added***************************************/
        //grab the text from all the EditText objects
        String  nameETText = nameET.getText().toString();
        String  accessPointNameETText = accessPointNameET.getText().toString();
        String  wpa2KeyETText = wpa2KeyET.getText().toString();
        String  basePathETText = basePathET.getText().toString();
        String  portNumberETText = portNumberET.getText().toString();
        
    	if(nameETText.isEmpty() || accessPointNameETText.isEmpty() || wpa2KeyETText.isEmpty() ||
    			basePathETText.isEmpty() || portNumberETText.isEmpty()){//at least one field is empty
    		
    		//create an alert dialog
    		AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddHardwareUnitActivity.this);
    		
    		//set the message to be displayed
            alertDialog.setMessage(R.string.dialog_message);
            
            //set behaviour of the button
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
        
    	/***********************************************************************************/
    	
    	
        // add new unit to db
        long huId = mHardwareUnitDataSource.addHardwareUnit(nameET.getText().toString(),
                                                            basePathET.getText().toString(),
                                                            Integer.parseInt(portNumberET.getText().toString()),
                                                            "access point name",
                                                            "wpa2key",
                                                            "btMac");
        Log.i("AddHardwareUnitActivity - addHardwareUnit()", "unit Id: " + huId);
        
        // send details to espruino via BT
        String accessPointName = accessPointNameET.getText().toString();
        String wpa2Key = wpa2KeyET.getText().toString();
        String portNumber = portNumberET.getText().toString();
        Log.i("AddHardwareUnitActivity - addHardwareUnit()", "Access Point: " + accessPointName + ", WPA2 Key: " + wpa2Key + ", Port Number: " + portNumber);
        
        if (mConnectedThread != null) {
            mConnectedThread.write(" ".getBytes());
            mConnectedThread.write("_set_apn".getBytes());
            mConnectedThread.write((accessPointName + "|").getBytes());
            mConnectedThread.write("_set_wpa".getBytes());
            mConnectedThread.write((wpa2Key + "|").getBytes());
            mConnectedThread.write("_set_port".getBytes());
            mConnectedThread.write((portNumber + "|").getBytes());
            mConnectedThread.write("_save".getBytes());
        } else {
            Log.i("AddHardwareUnitActivity - addHardwareUnit()", "mConnectedThread is null");
        }
               
        // close BT connection
        mConnectedThread.cancel();
        mConnectTask.cancel(true);
        
        // return to all units activity
        startAllUnitsActivity();
    }    
    
    /**
     * Async Task used to establish a Bluetooth connection with the HACK 
     * hardware unit.
     */
    private class ConnectToBluetoothTask extends AsyncTask<BluetoothDevice, Void, Integer> {
        
        private BluetoothSocket mmSocket;

        @Override
        protected Integer doInBackground(BluetoothDevice...bluetoothDevices) {
            BluetoothSocket tmp = null;
            BluetoothDevice device = bluetoothDevices[0];
            
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {                
                tmp = device.createRfcommSocketToServiceRecord(ESPRUINO_UUID);
            } catch (IOException e) { 
                // do something
                Log.i("ConnectToBluetoothTask - doInBackground()", "Failed to establish BT socket");
            }
            
            mmSocket = tmp;
            
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.i("ConnectToBluetoothTask - doInBackground()", "Unable to connect to BT device");
                
                try {
                    mmSocket.close();
                    this.cancel(true);
                    
                    // display dialog asking user to re-try connection
                    DialogFragment bluetoothConnectionErrorDialog = BluetoothConnectionErrorDialog.newInstance();
                    bluetoothConnectionErrorDialog.show(getFragmentManager(), "bluetooth_connection_error_dialog");
                } catch (IOException closeException) { 
                    // do something
                }
                
                return -1;
            }
            
            Log.i("ConnectToBluetoothTask - doInBackground()", "Connection successful!");
            
            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
            
            return 0;
        }
        
        // onPostExecute is invoked on UI thread and displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Integer result) {
            // enable "Add" button so user can submit form
            mAddHardwareUnitButton.setEnabled(true);
       }
        
        @Override
        protected void onCancelled(Integer result) {
            mAddHardwareUnitButton.setEnabled(false);
        }
    }
    
    /**
     * Creates a worker thread to manage the bluetooth connection.
     * Worker thread handles writes/reads of bytes to/from HACK hardware.
     */
    private synchronized void manageConnectedSocket(BluetoothSocket btSocket) {       
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
                    // TODO: Set up handler
                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
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
    
    // -- Bluetooth Connection Error Dialog
    
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
