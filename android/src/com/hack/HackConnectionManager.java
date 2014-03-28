package com.hack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.os.AsyncTask;
import android.util.Log;

public class HackConnectionManager {

    // -- Inner class that is used to perform submitRequest() asynchronously
    private class Dispatcher extends AsyncTask<HackCommand, Void, String> {

        private HackCommand mmCommand;

        public Dispatcher(HackCommand command) {
            super();
            mmCommand = command;
        }

        @Override
        protected void onPreExecute() {
            mmCommand.showProgressDialog();
        }

        /**
         * Chooses a strategy (Wifi or Bluetooth), then sends the command to the
         * appropriate HackConnectionAdapter.
         * @param long hardwareUnitId - the id of the hardware unit to send the request to
         * @param HackCommand command - the HackCommand.getUrl() should return a string of
         *  the form: "/hack/cmd?var1=foo&var2=bar" where cmd is the command you want to
         *  perform on the hardware. Make sure to pass in the correct query variables.
         *  For Wifi requests, this method will prepend "http://hostname:portnumber".
         *  You can get the hardware unit id from the command.
         */
        @Override
        protected String doInBackground(HackCommand... c) {
            HackCommand command = c[0]; // For now assume only 1 command was passed in
            String response = null;
            // choose BT adapter if available
            if (mBluetoothAdapter.isUnitAvailable(command)) {
                Log.i("HackConnectionManager - doInBackground()", "using BT...");
                translateToBluetooth(command);
                response = mBluetoothAdapter.submitRequest(command);
            } else {
            	translateToWifi(command);
                response = mWifiAdapter.submitRequest(command);
            }
            // need to set the Command response string in the Adapters
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            mmCommand.hideProgressDialog();
            Log.i("HackConnectionManager - onPostExecute()", "response: " + response);
            mmCommand.onPostExecute(response);
        }
    }

    private HackWifiAdapter mWifiAdapter;
    private HackBluetoothAdapter mBluetoothAdapter;

    public HackConnectionManager() {
        super();
        mBluetoothAdapter = new HackBluetoothAdapter();
        mWifiAdapter = new HackWifiAdapter();
    }

    public void submitRequest(HackCommand command) {
        // Initiate the command asynchronously
        new Dispatcher(command).execute(command);

        // Return control to UI before dispatch completes
    }


    // -- Private members

    void translateToBluetooth(HackCommand command) {
        String translation = command.getUrl();

        // If it has the Wifi prefix, remove it
        if (translation.startsWith("http://")) {
        	translation = translation.substring(6, translation.length() - 1);
        }
        
        // If it doesn't have the Bluetooth suffix, add it
        if (!translation.endsWith("$")) {
        	translation += "$";
        }

        command.setUrl(translation);
    }

    void translateToWifi(HackCommand command) {
    	String translation = command.getUrl();
    	
    	// If it doesn't have a Wifi prefix, add it
    	if (!translation.startsWith("http://")) {
    		translation = "http:/" + translation.substring(translation.indexOf("/"), translation.length() - 1);
    	}
    	
    	// If it has the Bluetooth suffix, remove it
    	if (translation.endsWith("$")) {
    		translation = translation.substring(0, translation.length() - 2);
    	}
    	
    	command.setUrl(translation);
    }
}
