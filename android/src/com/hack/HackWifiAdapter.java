package com.hack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
            new HttpConnectionTask(command).execute(command);
        } else {
            Toast.makeText(mContext.getApplicationContext(), 
                           "No network connection available", 
                           Toast.LENGTH_LONG).show();
        }
    }
    
    private String sendRequest(String strUrl) throws IOException {
        try {
            URL url = new URI(strUrl).toURL();
            Log.i("HackHttpConnectionManager - submitRequest()", "URL: " + url.toString());
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(10000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            
            // start the query
            conn.connect();
            int status = conn.getResponseCode();
            Log.i("HackHttpConnectionManager - submitRequest()", "The status is: " + status);
            
            switch (status) {
            case 200:  // OK?
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                String response = sb.toString();
                Log.i("HackWifiAdapter - sendRequest()", "JSON response: " + response);
                return response; 
            }            
              
        } catch (MalformedURLException e) {
            return null;
        } catch (URISyntaxException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    
    /* Uses AsyncTask to create a task away from the main UI thread. This task takes a 
     * URL string and uses it to create an HttpUrlConnection. Once the connection
     * has been established, the AsyncTask receives the response as an InputStream. Finally, 
     * the InputStream is converted into a JSON object, which is sent to the Activity in the 
     * AsyncTask's onPostExecute method. */
    private class HttpConnectionTask extends AsyncTask<HackCommand, Void, String> {
        
        private HackCommand mmCommand;
        
        public HttpConnectionTask(HackCommand command) {
            super();
            mmCommand = command;
        }
        
        @Override
        protected void onPreExecute() {
            mmCommand.onPreExecute();
        }
       
        @Override
        protected String doInBackground(HackCommand... commandUrls) {
            // commandUrls comes from the execute() call: commandUrls[0].getUrl() is the url as a String.
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
                if (response != null) {
                    mmCommand.onPostExecute(new JSONObject(response));
                } else {
                    mmCommand.onPostExecute(null);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           
        }
    }

}
