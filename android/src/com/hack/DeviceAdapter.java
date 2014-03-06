package com.hack;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DeviceAdapter extends ArrayAdapter<Device> {
    
    public DeviceAdapter(Context context, ArrayList<Device> devices) {
        super(context, R.layout.item_socket, devices);
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        // Get the data item for this position
        Device device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (v == null) {
           v = LayoutInflater.from(getContext()).inflate(R.layout.item_socket, parent, false);
        }
        // Lookup view for data population
        TextView socketLabel = (TextView) v.findViewById(R.id.socketLabel);
        ImageView socketImage = (ImageView) v.findViewById(R.id.socketImage);
        
        // Populate the data into the template view using the data object
        if (device != null) {
            socketLabel.setText(device.getName());
        } else {
            socketLabel.setText("");
        }
        // Return the completed view to render on screen
        return v;
    }

}
