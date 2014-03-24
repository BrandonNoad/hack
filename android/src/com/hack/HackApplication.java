package com.hack;

import android.app.Application;
import android.content.res.Configuration;

public class HackApplication extends Application {
    
    private HackConnectionManager mConnectionManager;
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
 
    @Override
    public void onCreate() {
        super.onCreate();
        mConnectionManager = new HackConnectionManager(this);
    }
 
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
 
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    
    public HackConnectionManager getConnectionManager() {
        return mConnectionManager;
    }
 

}
