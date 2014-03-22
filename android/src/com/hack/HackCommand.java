package com.hack;

import org.json.JSONObject;

public abstract class HackCommand {
    
    private HardwareUnit mHardwareUnit;
    private String mUrl;
    
    public HackCommand(HardwareUnit unit, String url) {
        mHardwareUnit = unit;
        mUrl = url;
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
    
    abstract void onResponseReceived(JSONObject json);

}
