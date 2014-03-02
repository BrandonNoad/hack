package com.hack;

public class HardwareUnit {
    private long mId;
    private String mName;
    private String mBasePath;
    private int mPortNumber;
    
    public HardwareUnit(long id, String name, String basePath, int portNumber) {
        mId = id;
        mName = name;
        mBasePath = basePath;
        mPortNumber = portNumber;               
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
    
     
}
