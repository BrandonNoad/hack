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

}
