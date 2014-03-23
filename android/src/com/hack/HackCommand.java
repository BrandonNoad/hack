package com.hack;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

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
    
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Sending Request...");
        mProgressDialog.setMessage("Please wait.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }
    
    public void onPostExecute(JSONObject json) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
