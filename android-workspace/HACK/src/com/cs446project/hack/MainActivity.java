package com.cs446project.hack;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.bluetooth.*;
import android.content.res.Resources;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create things defined in activity_main
        setContentView(R.layout.activity_main);
        m_t = (TextView)findViewById(R.id.msg);
        
        // Try and access bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        	m_t.setText(R.string.msg_noBT);
        } else {
        	m_t.setText(R.string.msg_yesBT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    public void test(View view) {

    }
    
    private TextView m_t;
}
