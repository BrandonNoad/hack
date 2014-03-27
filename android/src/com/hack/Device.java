package com.hack;

public class Device {

    private long mId;
    private long mHardwareUnitId;
    private long mSocketId;
    private String mName;
    private int mState;
    private long mTypeId;
    private String mType;
    private long mTotalTimeOn;  // milliseconds
    private long mOnSinceTime;  // millisecond date value

    public Device(long id, long hardwareUnitId, long socketId, String name, int state, long typeId, String type, long totalTimeOn, long onSinceTime) {
        mId = id;
        mHardwareUnitId = hardwareUnitId;
        mSocketId = socketId;
        mName = name;
        mState = state;
        mTypeId  = typeId;
        mType = type;
        mTotalTimeOn = totalTimeOn;
        mOnSinceTime = onSinceTime;
    }

    public long getId() {
        return mId;
    }

    public long getHardwareUnitId() {
        return mHardwareUnitId;
    }

    public long getSocketId() {
        return mSocketId;
    }

    public String getName() {
        return mName;
    }

    public int getState() {
        return mState;
    }

    public long getTypeId() {
        return mTypeId;
    }

    public String getType() {
        return mType;
    }

    // milliseconds
    public long getTotalTimeOn() {
        return mTotalTimeOn;
    }

    public long getOnSinceTime() {
        return mOnSinceTime;
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

    public void setTypeId(long typeId) {
        mTypeId = typeId;
    }
    public void setType(String type) {
        mType = type;
    }

    public void setTotalTimeOn(long time) {
        mTotalTimeOn = time;
    }

    public void setOnSinceTime(long time) {
        mOnSinceTime = time;
    }
}
