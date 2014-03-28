package com.hack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public abstract class HackCommand {

    private ProgressDialog mProgressDialog;
    private HardwareUnit mHardwareUnit;
    private String mUrl;
    private Context mContext;

    /**
     * 
     * @param context - activity context (not application context)
     * @param unit
     * @param url
     */
    public HackCommand(Context context, HardwareUnit unit, String url) {
        mHardwareUnit = unit;
        mUrl = url;
        mContext = context;
    }

    public String getUrl() {
        return mUrl;
    }

    public HardwareUnit getHardwareUnit() {
        return mHardwareUnit;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Context getContext() {
        return mContext;
    }

    public void showProgressDialog() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Sending Request...");
        mProgressDialog.setMessage("Please wait.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void onPreExecute() {
        // no-op - I'm making this a no-op, so we aren't forced to implement it        
    }

    /**
     * 
     * @param response - JSON string of the form {"success": 0/1, "data": { ... }, "message": "some message"} 
     */
    public void onPostExecute(String response) {
        // convert response to JSON
        try {
            JSONObject jsonResponse = new JSONObject(response);
            int isSuccess = jsonResponse.getInt("success");
            if (isSuccess != 0) {
                // success
                doSuccess(jsonResponse);
            } else {
                doFail(jsonResponse);
            }
        } catch (JSONException e) {
            JSONObject jsonResponse = new JSONObject();
            
            try {
                jsonResponse.put("message", "Error parsing response, got: " + response);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            doFail(jsonResponse);
        }

    }

    // must implement this when you define concrete command
    public void doSuccess(JSONObject response) {
        String command = "command";
        try {
            command = response.getString("command");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Log.i("HackComand - doSuccess()", "command: " + command);
        // show toast
        Toast.makeText(mContext.getApplicationContext(),  // must use application context
                "Got successful response from " + command,
                Toast.LENGTH_LONG).show();
    }

    public void doFail(JSONObject response) {
        String message;
        try {
            message = response.getString("message");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        
        Log.i("HackComand - doFail()", "message: " + message);
        // show toast
        Toast.makeText(mContext.getApplicationContext(),  // must use application context
                message,
                Toast.LENGTH_LONG).show();
    }

    public void send() {
        HackConnectionManager m = ((HackApplication)mContext.getApplicationContext()).getConnectionManager();
        m.submitRequest(this);
    }
}
