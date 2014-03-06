package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.Toast;

public class SingleUnitActivity extends Activity {
  
    public final static String EXTRA_HARDWARE_UNIT_ID = "com.hack.HARDWARE_UNIT_ID";
    public final static String EXTRA_SOCKET_ID = "com.hack.SOCKET_ID";
    public final static String EXTRA_DEVICE_ID = "com.hack.DEVICE_ID";
    public final static int NUM_SOCKETS = 4;
    
    String mBasePath = "http://192.168.43.239:8080/";
    
    private HardwareUnitDataSource mHardwareUnitDataSource;
    private DeviceDataSource mDeviceDataSource;
    private ArrayList<Device> mDevices = new ArrayList<Device>();
    private DeviceAdapter mDeviceAdapter;
    private long mHardwareUnitId;
    private ActionBar mActionBar;
    private Device mSelectedDevice = null;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_unit);
        // Show the Up button in the action bar.
        setupActionBar();
        mActionBar = getActionBar();
        
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        
        Intent intent = getIntent();
        mHardwareUnitId = intent.getLongExtra(AllUnitsActivity.EXTRA_UNIT_ID, -1);
        if (mHardwareUnitId >= 0) {
            displayUnit(mHardwareUnitId);
            displayDevices(mHardwareUnitId);
           
        }
        
        GridView socketGrid = (GridView) findViewById(R.id.socketGrid);
        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        socketGrid.setAdapter(mDeviceAdapter);
        
        socketGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                if (device != null) {
                    startDeviceDetailsActivity(device.getId());
                } else {
                    startAddDeviceActivity(position);
                }
            }
         });
        
        socketGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            // Called when the user long-clicks on someView
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Device device = (Device) parent.getItemAtPosition(position);
                if (device == null || mActionMode != null) {                       
                    return false;
                } else {                
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = SingleUnitActivity.this.startActionMode(mActionModeCallback);
                    v.setSelected(true);
                    mSelectedDevice = device;
                    return true;                  
                }
            }
        });
        
        Switch goStopButton = (Switch) findViewById(R.id.switchGoStop);
goStopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {      
                    ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new DownloadWebpageTask().execute(mBasePath + "go");
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available", 
                        Toast.LENGTH_LONG).show();
//                        message.setText("No network connection available.");
                    }                    
                } else {
                    ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new DownloadWebpageTask().execute(mBasePath + "stop");
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available", 
                        Toast.LENGTH_LONG).show(); 
//                        message.setText("No network connection available.");
                    }
                }
            }
        });
        
        
    }
    
    

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_unit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void displayUnit(long unitId) {
        HardwareUnit unit = mHardwareUnitDataSource.getHardwareUnitById(unitId);
        mActionBar.setTitle(unit.getName());
    }
    
    public void displayDevices(long unitId) {
        for (int i = 0; i < NUM_SOCKETS; i++) {
            Device d = mDeviceDataSource.getDevice(unitId, i);
            mDevices.add(d);
        }
    }
    
    public void deleteDevice(Device device) {
        mDeviceDataSource.deleteDeviceById(device.getId());
        mDevices.set((int) device.getSocketId(), null);
        mDeviceAdapter.notifyDataSetChanged();
    }
    
    public void startDeviceDetailsActivity(long deviceId) {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        startActivity(intent);
    }
    
    public void startAddDeviceActivity(long socketId) {
        Intent intent = new Intent(this, AddDeviceActivity.class);
        intent.putExtra(EXTRA_HARDWARE_UNIT_ID, mHardwareUnitId);
        intent.putExtra(EXTRA_SOCKET_ID, socketId);  
        startActivity(intent);
    }
    
    ActionMode mActionMode = null;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteDevice(mSelectedDevice);
                    mSelectedDevice = null; 
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
    
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URI(myurl).toURL();
            Log.i("URL:", url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i("Main Activity - get()", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, 500);
            return contentAsString;  
        }  catch(URISyntaxException e) {return "error";}
        finally {  // make sure input stream is closed
            if (is != null) {
                is.close();
            } 
        }        
    }
    
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");        
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
    
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a 
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
       @Override
       protected String doInBackground(String... urls) {
             
           // params comes from the execute() call: params[0] is the url.
           try {
               return downloadUrl(urls[0]);
           } catch (IOException e) {
               return "Unable to retrieve web page. URL may be invalid.";
           }
       }
       // onPostExecute displays the results of the AsyncTask.
       @Override
       protected void onPostExecute(String result) {
           Toast.makeText(getApplicationContext(), result, 
           Toast.LENGTH_LONG).show();     
//           message.setText(result);
      }
   }



}
