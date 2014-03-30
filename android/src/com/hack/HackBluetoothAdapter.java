package com.hack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class HackBluetoothAdapter extends HackConnectionAdapter {

    // -- Inner class that is used to interact with a connected Bluetooth socket
    private class BluetoothStream extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private ByteArrayOutputStream mmInBuffer;
        private boolean mmRunning;

        public BluetoothStream(BluetoothSocket socket) {
            Log.i("Connected Thread", "ctor starting");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                tmpIn = null;
                tmpOut = null;
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmInBuffer = new ByteArrayOutputStream();
            mmInBuffer.reset();

            Log.i("Connected Thread", "Ready to write bytes");
        }

        @Override
        public void run() {
            if (mmInStream == null || mmOutStream == null) {
                return;
            } else {
                mmRunning = true;
            }

            // Keep listening to the InputStream until an exception occurs
            while (mmRunning) {
                try {
                    // If there is something to read
                    if (mmInStream.available() > 0) {
                        mmInBuffer.write(mmInStream.read());
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
        
        public String read(long timeout) {
            long start = Calendar.getInstance().getTimeInMillis();
            int responseStart = 0, responseEnd = 0;
            String ret;
            
            while ((Calendar.getInstance().getTimeInMillis() - start) < timeout) {
                // Check if "/hack/" is in the buffer
                responseStart = mmInBuffer.toString().indexOf("/hack/");
                
                // If it is, check if "$" is in the buffer
                if (responseStart >= 0) {
                    responseEnd = mmInBuffer.toString().indexOf("$");

                    // If this is true, we have an entire command
                    if (responseEnd >= 0) {
                        ret = new String(mmInBuffer.toString().substring(responseStart, responseEnd)).replace("/hack/", "");
                        mmInBuffer.reset();
                        return ret;
                    }
                }

                yield();
            }
            
            return "";
        }
        
        public void done() {
            mmRunning = false;
            
            // Don't touch the mmStreams or mmSocket - let it be managed by BluetoothAdapter
        }
    }


    // -- Constants

    private static final UUID ESPRUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    // -- Member Variables

    private BluetoothAdapter mAdapter;
    private LinkedBlockingQueue<BluetoothDevice> mScanResult;
    private BluetoothSocket mSocket;
    private BluetoothStream mStream;
    private BluetoothDevice mLastDevice;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // When discovery finds a device
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                mScanResult.add(device);
            }
        }
    };


    // -- Public members

    public HackBluetoothAdapter() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanResult = new LinkedBlockingQueue<BluetoothDevice>();
        mSocket = null;
        mLastDevice = null;
    }

    public boolean isUnitAvailable(HackCommand c) {
        Log.i("HackBluetoothAdapter - isUnitAvailable()", "starting...");
        // If we are looking for a unit we haven't seen before
        if (c.getHardwareUnit().getBtMac().equals("")) {
            // Check if we have an unrecognized unit already paired
            for (BluetoothDevice d : getUnrecognizedPairedUnits(c)) {
                Log.i("BluetoothAdapter - isUnitAvailable()", "device MAC: " + d.getName());
                if (tryConnect(c, d)) {
                    Log.i("BluetoothAdapter - isUnitAvailable()", "tryConnect is successful");
                    // Update hardware unit with mac address
                    HardwareUnitDataSource db = new HardwareUnitDataSource(c.getContext()); 
                    db.open();
                    db.updateHardwareUnit(c.getHardwareUnit().getId(), d.getAddress());
                    db.close();
                    c.getHardwareUnit().setBtMac(d.getAddress());
                    return true;
                }
            }

            // Check if we see an unrecognized unit in a scan
            for (BluetoothDevice d : scanForNewUnits(c)) {
                if (tryConnect(c, d)) {
                    // Update hardware unit with mac address
                    HardwareUnitDataSource db = new HardwareUnitDataSource(c.getContext()); 
                    db.open();
                    db.updateHardwareUnit(c.getHardwareUnit().getId(), d.getAddress());
                    db.close();
                    c.getHardwareUnit().setBtMac(d.getAddress());
                    return true;
                }
            }
            // If we are looking for a unit we have seen before
        } else {
        	// If it's currently connected
        	if (mLastDevice != null && mSocket.isConnected()) {
        		if (mLastDevice.getAddress().equals(c.getHardwareUnit().getBtMac())) {
        			return true;
        		} else {
        			closeSocket();
        		}
        	}
        	
            // It's been paired with before
            if (isUnitRecognized(c.getHardwareUnit())) {
            	// Last device should have been set by isUnitRecogized, don't check for null on purpose
            	if (tryConnect(c, mLastDevice)) {
            		return true;
            	}
            }
        }
        return false;
    }

    @Override
    public String submitRequest(HackCommand command) {
        if (mSocket == null || !mSocket.isConnected()) {
            return fail("Call isUnitAvailable until returns true, then submit a request.");
        }

        mStream = new BluetoothStream(mSocket);
        mStream.start();

        if (mStream != null && mStream.isAlive()) {
            mStream.write(" ".getBytes());
            mStream.write(command.getUrl().getBytes());
        } else {
            mStream.done();
            
            closeSocket();
            return fail("Couldn't access stream thread");
        }

        // we need to stick around here and wait til we get a response (JSON string) from the espruino
        // then return this response
        String response = mStream.read(5000);
        
        if (response == "") {
            mStream.done();
            
            closeSocket();
            return fail("Timed out waiting for response");
        }

        // Technically the stream is a thread, so discard it at this point
        // Note that this should leave mSocket and its streams intact...
        mStream.done();

        return response;
    }


    // -- Private members
    
    private void closeSocket() {
        try {
            mSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        mLastDevice = null;
    }

    private boolean tryConnect(HackCommand c, BluetoothDevice d) {
        try {
            mSocket = d.createRfcommSocketToServiceRecord(ESPRUINO_UUID);
        } catch (IOException e) {
            mSocket = null;
            return false;
        }

        try {
            Log.i("here!","here");
            mAdapter.cancelDiscovery();
            Log.i("here!","discovery cancelled");
            mSocket.connect();
        } catch (IOException e) {
            Log.i("", "socket.connect() failed!");
            try {
                mSocket.close();
            } catch (IOException e1) {
                
            }

            mSocket = null;
            return false;
        }

        mLastDevice = d;
        return true;
    }

    private boolean isUnitRecognized(HardwareUnit u) {
        if (u.getBtMac() == "") {
            return false;
        }

        for (BluetoothDevice d : mAdapter.getBondedDevices()) {
            if (d.getAddress().equals(u.getBtMac())) {
            	// Set last device since we will try to connect to this
            	mLastDevice = d;
                return true;
            }
        }

        return false;
    }

    private ArrayList<BluetoothDevice> getUnrecognizedPairedUnits(HackCommand c) {
        HardwareUnitDataSource s = new HardwareUnitDataSource(c.getContext());
        s.open();

        ArrayList<String> recognized = new ArrayList<String>();
        ArrayList<BluetoothDevice> unrecognized = new ArrayList<BluetoothDevice>();

        for (HardwareUnit u : s.getAllHardwareUnits()) {
            recognized.add(u.getBtMac());
        }

        for (BluetoothDevice d : mAdapter.getBondedDevices()) {
            if (d.getName().equals("HC-05")) {
                if (!recognized.contains(d.getAddress())) {
                    unrecognized.add(d);
                }
            }
        }

        s.close();

        return unrecognized;
    }

    private ArrayList<BluetoothDevice> scanForNewUnits(HackCommand command) {
        ArrayList<BluetoothDevice> ret = new ArrayList<BluetoothDevice>();

        // Clear result member
        mScanResult.clear();

        // Set up a listener for discovery events
        command.getContext().registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // This starts low level discovery in a 3rd thread, separate from the UI one and this one
        mAdapter.startDiscovery();

        // Wait for discovery to complete
        while (mAdapter.isDiscovering()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        BluetoothDevice d;
        while (mScanResult.size() > 0) {
            try {
                d = mScanResult.take();

                // We are looking for any new hardware unit
                if (d.getName().equals("HC-05") && command.getHardwareUnit().getBtMac().equals("")) {
                    ret.add(d);
                    // We are looking for a specific hardware unit
                } else if (d.getName().equals("HC-05") && command.getHardwareUnit().getBtMac().equals(d.getAddress())) {
                    ret.add(d);
                }
            } catch (InterruptedException e) {
                //
            }
        }

        return ret;
    }
}
