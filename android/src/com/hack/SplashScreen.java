package com.hack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread splash = new Thread() {
            public void run() {

                try {
                    //sleep for 3 seconds
                    sleep(3 * 1000);

                    //after 3 seconds redirect to AllUnitsActivity
                    Intent intent = new Intent(getBaseContext(), AllUnitsActivity.class);
                    startActivity(intent);

                    finish();

                } catch (Exception e) {

                }
            }
        };

        // start thread
        splash.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash_screen, menu);
        return true;
    }

}
