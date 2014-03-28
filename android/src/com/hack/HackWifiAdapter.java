package com.hack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HackWifiAdapter extends HackConnectionAdapter {

    /**
     * 
     * @param url String - of the form: "http://basePath:PortNumber/hack/command?var=n"
     */
    // TODO: modify this to take a HackCommand object as param and pass to doInBackground()
    public String submitRequest(HackCommand command) {
        ConnectivityManager connMgr = (ConnectivityManager) command.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                return sendRequest(command.getUrl());
            } catch (IOException e) {
                return fail("Unable to connect to server. URL may be invalid.");
            }
        } else {
            return fail("No network connection available.");
        }
    }

    private String sendRequest(String strUrl) throws IOException {
        try {
            URL url = new URI(strUrl).toURL();
            Log.i("HackWifiAdapter - sendRequest()", "URL: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(10000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // start the query
            conn.connect();
            int status = conn.getResponseCode();
            Log.i("HackWifiAdapter - submitRequest()", "The status is: " + status);

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
            default:
                return fail(status + " error");
            } 
        } catch (MalformedURLException e) {
            return fail("Error. Malformed URL. Please try again.");
        } catch (URISyntaxException e) {
            return fail("Error. URI Syntax Exception. Please try again.");
        } catch (IOException e) {
            return fail("Error. Unable to connect to server. Please try again.");
        }

    }

}
