package com.hack;

public class Timer {
    
    private long mId;
    private long mDeviceId;
    private String mTimeOn;
    private String mTimeOff;
    private boolean mIsRepeated;
    
    public Timer(long id, long deviceId, String timeOn, String timeOff, boolean isRepeated) {
        mId = id;
        mDeviceId = deviceId;
        mTimeOn = timeOn;
        mTimeOff = timeOff;
        mIsRepeated = isRepeated;
    }
    
    public long getId() {
        return mId;
    }
    
    public long getDeviceId() {
        return mDeviceId;
    }
    
    public String getTimeOn() {
        return mTimeOn;
    }
    
    public String getTimeOff() {
        return mTimeOff;
    }
    
    public boolean getIsRepeated() {
        return mIsRepeated;
    }

}
