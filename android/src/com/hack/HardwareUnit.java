package com.hack;

public class HardwareUnit {
    private long mId;
    private String mName;
    
    public long getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setId(long id) {
        mId = id;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public HardwareUnit(long id, String name) {
        mId = id;
        mName = name;
    }

}
