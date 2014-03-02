package com.hack;

public class Device {
    private long mId;
    private long mHardwareUnitId;
    private long mSocketId;
    private String mName;
    private int mState;
    
    public Device(long id, long hardwareUnitId, long socketId, String name, int state) {
        mId = id;
        mHardwareUnitId = hardwareUnitId;
        mSocketId = socketId;
        mName = name;
        mState = state;
    }
    
    public long getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }
    
    public int getState() {
        return mState;
    }
    
    public void setId(long id) {
        mId = id;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public void setState(int state) {
        mState = state;
    }
}
