package com.hack;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This activity establishes a Bluetooth connection with a HACK hardware unit
 * and displays a form allowing the user to add a new hardware unit. The form
 * details are sent to the HACK hardware unit via Bluetooth, which initializes
 * the HACK harware unit web server.
 */
public class AddHardwareUnitActivity extends Activity {
    
    // -- Constants
    
    // -- Member Variables
    
    private Button mAddHardwareUnitButton;
    private HardwareUnitDataSource mHardwareUnitDataSource;
    private BluetoothDevice mChosenBluetooth;
    
    // -- Initialize Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hardware_unit);

        setupActionBar();  // show "up" button in action bar
        
        // initialize members
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();        
        mAddHardwareUnitButton = (Button) findViewById(R.id.add_hardware_unit_button);
        
        // get BluetoothDevice passed in from previoius activity
        Bundle bundle = getIntent().getExtras();
        mChosenBluetooth = bundle.getParcelable(AllUnitsActivity.EXTRA_BT_DEVICE);
        
        // set up event listeners
        mAddHardwareUnitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addHardwareUnit();
            }
        });
    }
    
    // -- Action Bar

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_hardware_unit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -- Intents
    
    public void startAllUnitsActivity() {
        // return to All Units Activity
        Intent intent = new Intent(this, AllUnitsActivity.class);
        startActivity(intent);
    }
    
    // -- UI Actions
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
    
    public void addHardwareUnit() {
        EditText nameET = (EditText) findViewById(R.id.editTextHardwareUnitName);
        EditText accessPointNameET = (EditText) findViewById(R.id.editTextAccessPointName);
        EditText wpa2KeyET = (EditText) findViewById(R.id.editTextWpa2Key);
        EditText basePathET = (EditText) findViewById(R.id.editTextBasePath);
        EditText portNumberET = (EditText) findViewById(R.id.editTextPortNumber);
        
        // add new unit to db
        long huId = mHardwareUnitDataSource.addHardwareUnit(nameET.getText().toString(),
                                                            basePathET.getText().toString(),
                                                            Integer.parseInt(portNumberET.getText().toString()),
                                                            accessPointNameET.getText().toString(),
                                                            wpa2KeyET.getText().toString(),
                                                            mChosenBluetooth.getAddress());
        Log.i("AddHardwareUnitActivity - addHardwareUnit()", "unit Id: " + huId);

        String accessPointName = accessPointNameET.getText().toString();
        String wpa2Key = wpa2KeyET.getText().toString();
        String portNumber = portNumberET.getText().toString();
        Log.i("AddHardwareUnitActivity - addHardwareUnit()", "Access Point: " + accessPointName + ", WPA2 Key: " + wpa2Key + ", Port Number: " + portNumber);
        
        Context context = getApplicationContext();
        CharSequence text = "BT MAC: " + mChosenBluetooth.getAddress() + " UNIT NAME: " + nameET.getText() + " AP NAME: " + accessPointName + " KEY: " + wpa2Key + " PORT: " + portNumber;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        
        // return to all units activity
        startAllUnitsActivity();
    }
}
