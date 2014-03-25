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
                JSONObject data = jsonResponse.getJSONObject("data");
                doSuccess(data);
            } else {
                String message = jsonResponse.getString("message");
                doFail(message);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     
    }
    
    // must implement this when you define concrete command
    abstract void doSuccess(JSONObject data);

    public void doFail(String message) {
        Log.i("HackComand - doFail()", "message: " + message);
        // show toast
        Toast.makeText(mContext.getApplicationContext(),  // must use application context
                       message,
                       Toast.LENGTH_LONG).show();
    }

}
