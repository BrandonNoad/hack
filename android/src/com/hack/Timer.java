package com.hack;

public class Timer {

    private long mId;
    private long mDeviceId;
    private long mTimeOn;
    private long mTimeOff;
    private boolean mIsRepeated;

    public Timer(long id, long deviceId, long timeOn, long timeOff, boolean isRepeated) {
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

    public long getTimeOn() {
        return mTimeOn;
    }

    public long getTimeOff() {
        return mTimeOff;
    }

    public boolean getIsRepeated() {
        return mIsRepeated;
    }

}
