package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public BluetoothStream(BluetoothSocket socket) {
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

        @Override
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


    // -- Constants

    private static final UUID ESPRUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    // -- Member Variables

    private BluetoothAdapter mAdapter;
    private LinkedBlockingQueue<BluetoothDevice> mScanResult;
    private BluetoothSocket mSocket;
    private BluetoothStream mStream;
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
    }

    public boolean isUnitAvailable(HackCommand c) {
        Log.i("HackBluetoothAdapter - isUnitAvailable()", "starting...");
        // If we are looking for a unit we haven't seen before
        if (c.getHardwareUnit().getBtMac().equals("")) {
            // Check if we have an unrecognized unit already paired
            for (BluetoothDevice d : getUnrecognizedPairedUnits(c)) {
                if (tryConnect(c, d)) {
                    return true;
                }
            }

            // Check if we see an unrecognized unit in a scan
            for (BluetoothDevice d : scanForNewUnits(c)) {
                if (tryConnect(c, d)) {
                    return true;
                }
            }
            // If we are looking for a unit we have seen before
        }/* else {
    		// It's been paired with before
    		if (isUnitRecognized(c.getHardwareUnit())) {
    			return true;
    		}
    	}
         */
        return false;
    }

    @Override
    public String submitRequest(HackCommand command) {
        if (mSocket == null || !mSocket.isConnected()) {
            return fail("Call isUnitAvailable until returns true, then submit a request.");
        }

        mStream = new BluetoothStream(mSocket);
        mStream.start();

        if (mStream != null) {
            mStream.write(" ".getBytes());
            mStream.write(command.getUrl().getBytes());
        } else {
            return fail("Couldn't access stream thread");
        }

        // we need to stick around here and wait til we get a response (JSON string) from the espruino
        // then return this response

        // Technically the stream is a thread, so discard it at this point
        mStream.cancel();

        return "{'success': 1, 'data': {}, 'message':'Success!'}";
    }


    // -- Private members

    private boolean tryConnect(HackCommand c, BluetoothDevice d) {
        try {
            mSocket = d.createRfcommSocketToServiceRecord(ESPRUINO_UUID);
        } catch (IOException e) {
            mSocket = null;
            return false;
        }

        try {
            mSocket.connect();
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e1) {
                //
            }

            mSocket = null;
            return false;
        }

        return true;
    }

    private boolean isUnitRecognized(HardwareUnit u) {
        if (u.getBtMac() == "") {
            return false;
        }

        for (BluetoothDevice d : mAdapter.getBondedDevices()) {
            if (d.getAddress().equals(u.getBtMac())) {
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
