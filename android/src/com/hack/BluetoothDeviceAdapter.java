package com.hack;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        
    public BluetoothDeviceAdapter(Context context, ArrayList<BluetoothDevice> bluetoothDevices) {
        super(context, R.layout.item_bluetooth_device, bluetoothDevices);
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        // Get the data item for this position
        BluetoothDevice btDevice = getItem(position);    
        // Check if an existing view is being reused, otherwise inflate the view
        if (v == null) {
           v = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetooth_device, null);
        }
        // Lookup view for data population
        TextView bluetoothDeviceName = (TextView) v.findViewById(R.id.bluetoothDeviceName);
        // Populate the data into the template view using the data object
        bluetoothDeviceName.setText(btDevice.getName());
        // Return the completed view to render on screen
        return v;
    }


}
