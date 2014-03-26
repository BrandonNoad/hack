package com.hack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HackConnectionManager {

	// -- Inner class that is used to perform submitRequest() asynchronously
	private class Dispatcher extends AsyncTask<HackCommand, Void, HackCommand> {

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
		protected HackCommand doInBackground(HackCommand... c) {
			HackCommand command = c[0]; // For now assume only 1 command was passed in

			// choose BT adapter if available
			if (mBluetoothAdapter.isUnitAvailable(command)) {
				translateToBluetooth(command);
				command.setTest(command.getUrl());
				mBluetoothAdapter.submitRequest(command);
			} else {
				translateToWifi(command);
				mWifiAdapter.submitRequest(command);
			}

			return command;
		}

		@Override
		protected void onPostExecute(HackCommand c) {
			JSONObject response = new JSONObject();
			try {
				response.put("result", c.getTest());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Report back to UI
			c.onResponseReceived(response);
		}
	}

	private HackWifiAdapter mWifiAdapter;
	private HackBluetoothAdapter mBluetoothAdapter;

	public HackConnectionManager(Context context) {
		super();
		mBluetoothAdapter = new HackBluetoothAdapter();
		mWifiAdapter = new HackWifiAdapter(context);
	}

	public void submitRequest(HackCommand command) {
		// Initiate the command asynchronously
		new Dispatcher().execute(command);

		// Return control to UI before dispatch completes
	}


	// -- Private members

	void translateToBluetooth(HackCommand command) {
		URI uriCommand = null;
		try {
			uriCommand = new URI("http://noop.com" + command.getUrl());
		} catch (URISyntaxException e1) {
			command.returnEarly("Couldn't parse the command url");
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

	void translateToWifi(HackCommand command) {
		// prepend host name and port number to url
		HardwareUnit unit = command.getHardwareUnit();
		if (unit != null) {
			String oldUrl = command.getUrl();
			String newUrl = "http://" + unit.getBasePath() + ":" + unit.getPortNumber() + oldUrl;
			Log.i("HackConnectionManager - submitRequest()", "newUrl: " + newUrl);
			command.setUrl(newUrl);
		}
	}
}
