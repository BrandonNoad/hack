package com.hack;

import org.json.JSONObject;

public interface HackCommand {
    
    void onResponseReceived(JSONObject json);

}
