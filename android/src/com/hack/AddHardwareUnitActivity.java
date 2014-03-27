package com.hack;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddHardwareUnitActivity extends Activity {

    // -- Commands this class may send
    public class SyncWifiCommand extends HackCommand {
        public SyncWifiCommand(HardwareUnit unit, Context context) {
            super(context, unit, "");

            String url = "http://noop/hack/sync?name=" + unit.getName() +
                    "&ap=" + unit.getAcessPointName() +
                    "&key=" + unit.getWpa2Key() +
                    "&basePath=" + unit.getBasePath() +
                    "&port=" + String.valueOf(unit.getPortNumber());

            setUrl(url);
        }


        @Override
        public void doSuccess(JSONObject data, String message) {
            super.doSuccess(data, message);
            // should we wait for success before we update the db?
            // return to all units activity
            startAllUnitsActivity();
        }
    }

    // -- Constants

    // -- Member Variables

    private Button mAddHardwareUnitButton;
    private HardwareUnitDataSource mHardwareUnitDataSource;
    //private BluetoothDevice mChosenBluetooth;

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
        //Bundle bundle = getIntent().getExtras();
        //mChosenBluetooth = bundle.getParcelable(AllUnitsActivity.EXTRA_BT_DEVICE);

        // set up event listeners
        mAddHardwareUnitButton.setOnClickListener(new OnClickListener() {
            @Override
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
        String unitName = ((EditText)findViewById(R.id.editTextHardwareUnitName)).getText().toString();
        String apName = ((EditText)findViewById(R.id.editTextAccessPointName)).getText().toString();
        String key = ((EditText)findViewById(R.id.editTextWpa2Key)).getText().toString();
        String basePath = ((EditText)findViewById(R.id.editTextBasePath)).getText().toString();
        String port = ((EditText)findViewById(R.id.editTextPortNumber)).getText().toString();

        // add new unit to db
        // should we wait until we get a successful response back from espruino before adding? i.e. do in command doSuccess() instead?
        long huId = mHardwareUnitDataSource.addHardwareUnit(unitName, basePath, Integer.parseInt(port), apName, key, "");

        // send discover command
        HackCommand syncWifiCommand = new SyncWifiCommand(mHardwareUnitDataSource.getHardwareUnitById(huId), this);
        syncWifiCommand.send();
    }
}
