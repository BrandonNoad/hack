package com.hack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HackWifiAdapter extends HackConnectionAdapter {
    
    private Context mContext;
    
    public HackWifiAdapter(Context context) {
        mContext = context;
    }
    
    /**
     * 
     * @param url String - of the form: "http://basePath:PortNumber/hack/command?var=n"
     */
    // TODO: modify this to take a HackCommand object as param and pass to doInBackground()
    public void submitRequest(HackCommand command) {
        ConnectivityManager connMgr = (ConnectivityManager)
        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // start async task
            new HttpConnectionTask().execute(command);
        } else {
            Toast.makeText(mContext.getApplicationContext(), 
                           "No network connection available", 
                           Toast.LENGTH_LONG).show();
        }
    }
    
    // Reads an InputStream and converts it to a String.
    private String readInputStream(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedInputStream bufStream = new BufferedInputStream(stream);
        Reader reader = null;
        reader = new InputStreamReader(bufStream, "UTF-8");        
        char[] buffer = new char[1024];
        reader.read(buffer);
        return new String(buffer);
    }
    
    private String sendRequest(String strUrl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URI(strUrl).toURL();
            Log.i("HackHttpConnectionManager - submitRequest()", "URL: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // start the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i("HackHttpConnectionManager - submitRequest()", "The response is: " + response);
            is = conn.getInputStream();

            // convert the InputStream into a string
            String contentAsString = readInputStream(is);
            return contentAsString;  
        } catch (URISyntaxException e) {
            return "error";
        } finally {  // make sure input stream is closed
            if (is != null) {
                is.close();
            } 
        }
    }
    
    /* Uses AsyncTask to create a task away from the main UI thread. This task takes a 
     * URL string and uses it to create an HttpUrlConnection. Once the connection
     * has been established, the AsyncTask receives the response as an InputStream. Finally, 
     * the InputStream is converted into a JSON object, which is sent to the Activity in the 
     * AsyncTask's onPostExecute method. */
    private class HttpConnectionTask extends AsyncTask<HackCommand, Void, String> {
        
        private HackCommand mmCommand;
       
        @Override
        protected String doInBackground(HackCommand... commandUrls) {
            
            mmCommand = commandUrls[0];
             
            // commandUrls comes from the execute() call: commandUrls[0].toUrl() is the url as a String.
            try {
                return sendRequest(commandUrls[0].getUrl());
            } catch (IOException e) {
                return "{'success': '0', 'message': 'Unable to retrieve web page. URL may be invalid.'}";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String response) {
            Log.i("HackWifiAdapter - onPostExecute()", "response: " + response);
            try {
                mmCommand.onResponseReceived(new JSONObject(response));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           
        }
    }

}
