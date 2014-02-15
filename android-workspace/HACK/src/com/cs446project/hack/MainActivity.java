package com.cs446project.hack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
//import android.widget.Button;
import android.widget.TextView;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MainActivity extends Activity {

	public enum State {
		INIT, ADAPTER_MISSING, ADAPTER_OFF, ADAPTER_ON, SEARCHING, ADAPTER_READY, CONNECTED
	}
	
	private class ConnectThread extends HandlerThread {
	    public ConnectThread(Handler parent, BluetoothDevice device) {
	    	super("ConnectThread::"+device.getName());
	    	m_parent = parent;
	    	
	        // Use a temporary object that is later assigned
	        /*BluetoothSocket tmp = null;
	        m_device = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
	        } catch (IOException e) {}
	        m_socket = tmp;*/
	    }
	 
	    public void run() {	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            m_socket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                m_socket.close();
	            } catch (IOException closeException) {}
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);
	    }
	 
	    /*public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }*/
	    
	    //private final BluetoothSocket m_socket;
	    //private final BluetoothDevice m_device;
	    private final Handler m_parent;
	    private final Handler m_handler = new Handler(getLooper()) {
	    	@Override
	        public void handleMessage(Message msg) {
	    		
	        }
	    };
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize simple members
        m_state = State.INIT;
        m_devices = new ArrayList<BluetoothDevice>();
        
        // Create things defined in activity_main
        setContentView(R.layout.activity_main);
        m_t = (TextView)findViewById(R.id.msg);
        
        // Reference Bluetooth and register for broadcasts
        m_BTAdapter = BluetoothAdapter.getDefaultAdapter();
        this.registerReceiver(m_receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        this.registerReceiver(m_receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        this.registerReceiver(m_receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        this.registerReceiver(m_receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        
        // Initialize tool
        //nextState(null);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
        // Unregister broadcast listeners
        this.unregisterReceiver(m_receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void nextState(View view) {
    	switch (m_state) {
    	  case INIT:
    	  case ADAPTER_MISSING:
    		detectBluetooth();
    		break;
    	  case ADAPTER_OFF:
    		enableBluetooth();
			break;
		  case ADAPTER_ON:
			startDiscovery();
			break;
		  case ADAPTER_READY:
			tryConnect();
			break;
		  case CONNECTED:
			break;
		  case SEARCHING:
		  default:
			break;
    	}
    }
    
    public void detectBluetooth() {
        // Try and access bluetooth
        m_t.append("\n"+getResources().getString(R.string.msg_checkBT));
        if (m_BTAdapter == null) {
            // Device does not support Bluetooth
        	m_t.append("\n"+getResources().getString(R.string.msg_noBT));
        	m_state = State.ADAPTER_MISSING;
        } else {
        	enableBluetooth();
        }
    }
    
    public void enableBluetooth() {
    	m_t.append("\n"+getResources().getString(R.string.msg_enablingBT));
    	if (!m_BTAdapter.isEnabled()) {
    	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	} else {
        	m_t.append("\n"+getResources().getString(R.string.msg_BTon));
        	m_state = State.ADAPTER_ON;
    	}
    }
    
    public void startDiscovery() {
    	m_t.append("\n"+getResources().getString(R.string.msg_reqSearch));
    	m_BTAdapter.startDiscovery();
    }
    
    public void tryConnect() {
    	m_t.append("\n"+getResources().getString(R.string.msg_tryConnect));
    	m_connectThread = new ConnectThread(m_connectThreadHandler, m_devices.get(0));
    	//m_connectThread.start();
    	
    	/*BluetoothDevice device = m_devices.get(0);
    	
    	m_t.append("\n"+getResources().getString(R.string.msg_tryConnect));
    	m_t.append(device.getName()+" ("+device.getAddress()+")");
    	
        // Use a temporary object that is later assigned
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
            m_t.append("\n"+getResources().getString(R.string.msg_yesConnect));
            m_socket = tmp;
            m_state = State.CONNECTED;
        } catch (IOException e) {
        	m_t.append("\n"+getResources().getString(R.string.msg_noConnect));
        }*/
    }
    
    private State m_state;
    private TextView m_t;
    //private Button m_b;
    private BluetoothAdapter m_BTAdapter;
    private BluetoothSocket m_socket;
    private ArrayList<BluetoothDevice> m_devices;
    private ConnectThread m_connectThread;
    private final int REQUEST_ENABLE_BT = 1; // This is necessary for some dumb reason
    
    private final BroadcastReceiver m_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // When the bluetooth adapter turns on or off
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                
                switch (state) {
                  case BluetoothAdapter.STATE_OFF:
                	m_t.append("\n"+getResources().getString(R.string.msg_BToff));
                	m_state = State.ADAPTER_OFF;
                    break;
                  case BluetoothAdapter.STATE_ON:
                	m_t.append("\n"+getResources().getString(R.string.msg_BTon));
                	m_state = State.ADAPTER_ON;
                    break;
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
            	m_t.append("\n"+getResources().getString(R.string.msg_searchStart));
            	m_state = State.SEARCHING;
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            	m_t.append("\n"+getResources().getString(R.string.msg_searchDone));
            	
            	if (m_devices.isEmpty()) {
            		m_state = State.ADAPTER_ON;
            	} else {
            		m_state = State.ADAPTER_READY;
            	}
            // When discovery finds a device
            } else if (m_state == State.SEARCHING && action.equals(BluetoothDevice.ACTION_FOUND)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                m_t.append("\n"+device.getName()+" ("+device.getAddress()+")");
                m_devices.add(device);
            }
        }
    };
    
    private final Handler m_connectThreadHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    		
        }
    };
}
