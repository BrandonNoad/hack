package com.hack;

import org.json.JSONObject;

public interface HackResponseCommand {
    
    void onResponseReceived(JSONObject json);

}
