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
                command.setTest(command.getUrl());
                response = mBluetoothAdapter.submitRequest(command);
            } else {
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
        URI uriCommand = null;
        try {
            uriCommand = new URI(command.getUrl());
        } catch (URISyntaxException e1) {
            mBluetoothAdapter.fail("Couldn't parse the command url");
        }

        String[] strCommand = uriCommand.getPath().split("/");
        List<NameValuePair> getMap = URLEncodedUtils.parse(uriCommand, "UTF-8");
        String translation = "";

        for (String commandPiece : strCommand) {
            if (!commandPiece.equals("")) {
                translation += "_" + commandPiece + "|";
            }
        }

        for (NameValuePair entry : getMap) {
            translation += "_set_" + entry.getName() + entry.getValue() + "|";
        }

        translation += "$"; // End of command delimiter for bluetooth

        command.setUrl(translation);
    }

}
