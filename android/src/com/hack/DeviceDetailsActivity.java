package com.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DeviceDetailsActivity extends Activity {
    
    long mDeviceId;
    Device mDevice;
    DeviceDataSource mDeviceDataSource;
    int mDeviceState;
    TextView message;
    String mBasePath = "http://brandonnoad.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);
        // Show the Up button in the action bar.
        setupActionBar();
        
        ToggleButton deviceStateToggle = (ToggleButton) findViewById(R.id.deviceStateToggleButton);
        message = (TextView) findViewById(R.id.res);
        
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        
        Intent intent = getIntent();
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        
        mDevice = mDeviceDataSource.getDeviceById(mDeviceId);
        if (mDevice != null) {
            mDeviceState = mDevice.getState();
            if (mDeviceState == 0) {
                deviceStateToggle.setChecked(false);
            } else {
                deviceStateToggle.setChecked(true);
            }
        }
        
        deviceStateToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {      
                    ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new DownloadWebpageTask().execute(mBasePath + "on");
                    } else {
                        message.setText("No network connection available.");
                    }                    
                } else {
                    ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new DownloadWebpageTask().execute(mBasePath + "off");
                    } else {
                        message.setText("No network connection available.");
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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
  
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
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
        } finally {  // make sure input stream is closed
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
           message.setText(result);
      }
   }
}
