package com.hack;

import android.content.Context;
import android.util.Log;

public class HackConnectionManager {
    
    private HackWifiAdapter mWifiAdapter;
    private HackBluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    
    public HackConnectionManager(Context context) {
        super();
        mWifiAdapter = new HackWifiAdapter(context);
//        mBluetoothAdapter = new HackBluetoothAdapter(context);s
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
    public void submitRequest(HackCommand command) {
        // choose Wifi or BT adapter
        
        // assume Wifi for now
        if (true) {
            // prepend host name and port number to url
            HardwareUnit unit = command.getHardwareUnit();
            if (unit != null) {
                String oldUrl = command.getUrl();
                String newUrl = "http://" + unit.getBasePath() + ":" + unit.getPortNumber() + oldUrl;
                Log.i("HackConnectionManager - submitRequest()", "newUrl: " + newUrl);
                command.setUrl(newUrl);
                mWifiAdapter.submitRequest(command);
            }
        }
    }

}
