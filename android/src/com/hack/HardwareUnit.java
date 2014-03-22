package com.hack;

public class HardwareUnit {
    
    private long mId;
    private String mName;
    private String mBasePath;
    private int mPortNumber;
    private String mAccessPointName;
    private String mWpa2Key;
    private String mBtMac;
    
    public HardwareUnit(long id, String name, String basePath, int portNumber, String accessPointName, String wpa2Key, String btMac) {
        mId = id;
        mName = name;
        mBasePath = basePath;
        mPortNumber = portNumber;
        mAccessPointName = accessPointName;
        mWpa2Key = wpa2Key;
        mBtMac = btMac;
    }   
    
    public long getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getBasePath() {
        return mBasePath;
    }
    
    public int getPortNumber() {
        return mPortNumber;
    }
    
    public String getAcessPointName() {
        return mAccessPointName;
    }
    
    public String getWpa2Key() {
        return mWpa2Key;
    }
    
    public String getBtMac() {
        return mBtMac;
    }
    
    public void setId(long id) {
        mId = id;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public void setBasePath(String basePath) {
        mBasePath = basePath;
    }
    
    public void setPortNumber(int portNumber) {
        mPortNumber = portNumber;
    }    
    
    public void setAccessPointName(String accessPointName) {
        mAccessPointName = accessPointName;
    }
    
    public void setWpa2Key(String wpa2Key) {
        mWpa2Key = wpa2Key;
    }
     
}
