package com.hack;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HardwareUnitAdapter extends ArrayAdapter<HardwareUnit> {
    
    public HardwareUnitAdapter(Context context, ArrayList<HardwareUnit> hardwareUnits) {
        super(context, R.layout.item_hardware_unit, hardwareUnits);
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        // Get the data item for this position
        HardwareUnit unit = getItem(position);    
        // Check if an existing view is being reused, otherwise inflate the view
        if (v == null) {
           v = LayoutInflater.from(getContext()).inflate(R.layout.item_hardware_unit, null);
        }
        // Lookup view for data population
        TextView hardwareUnitName = (TextView) v.findViewById(R.id.hardwareUnitName);
        // Populate the data into the template view using the data object
        hardwareUnitName.setText(unit.getName());
        // Return the completed view to render on screen
        return v;
    }

}
