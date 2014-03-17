package com.hack;

public class Device {
    
    private long mId;
    private long mHardwareUnitId;
    private long mSocketId;
    private String mName;
    private int mState;
    private String mType;
    // private ? mLastTimeOn  would this be a date?
    private int mTotalTimeOn;
    
    public Device(long id, long hardwareUnitId, long socketId, String name, int state, String type, int totalTimeOn) {
        mId = id;
        mHardwareUnitId = hardwareUnitId;
        mSocketId = socketId;
        mName = name;
        mState = state;
        mType = type;
        mTotalTimeOn = totalTimeOn;
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
    
    public long getSocketId() {
        return mSocketId;
    }
    
    public long getHardwareUnitId() {
        return mHardwareUnitId;
    }
    
    public String getType() {
        return mType;
    }
    
    // hours? minutes?
    public int getTotalTimeOn() {
        return mTotalTimeOn;
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
    
    public void setType(String type) {
        mType = type;
    }
    
    public void setTotalTimeOn(int time) {
        mTotalTimeOn = time;
    }
}
