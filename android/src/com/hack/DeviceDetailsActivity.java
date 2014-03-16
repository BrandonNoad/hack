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
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DeviceDetailsActivity extends Activity {
    
    // -- Member Variables 
    
    private long mDeviceId;
    private Device mDevice;
    private DeviceDataSource mDeviceDataSource;
    private long mSocketId;
    private int mDeviceState;
    
    private HardwareUnit mHardwareUnit;
    private HardwareUnitDataSource mHardwareUnitDataSource;
    
    private String mBasePath = "";
    private int mPortNumber = 80;
    private ActionBar mActionBar;

    // -- Initialize Activity
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);
        // Show the Up button in the action bar.
        setupActionBar();
        mActionBar = getActionBar();
                
        // initialize memebers
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();
        
        // get device id from previous activity
        Intent intent = getIntent();
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        
        
        ToggleButton deviceStateToggle = (ToggleButton) findViewById(R.id.deviceStateToggleButton);
        
        // TODO: update device state from server response
        mDevice = mDeviceDataSource.getDeviceById(mDeviceId);
        if (mDevice != null) {
            mActionBar.setTitle(mDevice.getName());
            mDeviceState = mDevice.getState();
            if (mDeviceState == 0) {
                deviceStateToggle.setChecked(false);
            } else {
                deviceStateToggle.setChecked(true);
            }
            mSocketId = mDevice.getSocketId();
            mHardwareUnit = mHardwareUnitDataSource.getHardwareUnitById(mDevice.getHardwareUnitId());
            mBasePath = mHardwareUnit.getBasePath();
            mPortNumber = mHardwareUnit.getPortNumber();
        }
        
        // set up event listeners
        deviceStateToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    turnOnDevice();
                } else {
                   turnOffDevice();
                }
            }
        });
        
        Button showTimePickerButton = (Button) findViewById(R.id.setTimeButton);
        
        showTimePickerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        
        
    }
    
    // -- Action Bar
    
    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device_details, menu);
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
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra(AllUnitsActivity.EXTRA_UNIT_ID, mDevice.getHardwareUnitId());            
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
  
    // -- Communicate with Espruino
    
    // TODO: Clean up this whole section; rename Async Task and method names
    
    public void turnOnDevice() {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String url = "http://" + mBasePath + ":" + mPortNumber + "/hack/on?socket=" + mSocketId;
            Log.i("URL", url);
            // start async task
            new DownloadWebpageTask().execute(url);
//                    new DownloadWebpageTask().execute(mBasePath + mDevice.getSocketId() + "/on");
        } else {
            Toast.makeText(getApplicationContext(), "No network connection available", 
            Toast.LENGTH_LONG).show();
        }
    }
    
    public void turnOffDevice() {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String url = "http://" + mBasePath + ":" + mPortNumber + "/hack/off?socket=" + mSocketId;
            Log.i("URL", url);
            // start async task
            new DownloadWebpageTask().execute(url);
//                    new DownloadWebpageTask().execute(mBasePath + mDevice.getSocketId() + "/off");
        } else {
            Toast.makeText(getApplicationContext(), "No network connection available", 
            Toast.LENGTH_LONG).show(); 
        }
    }
    
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
            String contentAsString = readIt(is, 500);  // TODO: change 500
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
            // display response string
            Toast.makeText(getApplicationContext(), result, 
            Toast.LENGTH_LONG).show();     
        }
    }
    
    // -- Time Picker Dialog
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }   
    
    
    public static class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                                        DateFormat.is24HourFormat(getActivity()));
        }
        
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        }
    }
    
}
