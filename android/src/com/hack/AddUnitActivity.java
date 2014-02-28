package com.hack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddUnitActivity extends Activity {
    
    public final static String EXTRA_UNIT_ID = "com.hack.UNIT_ID";
    
    private HardwareUnitDataSource mHardwareUnitsDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unit);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mHardwareUnitsDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitsDataSource.open();
        
        Button addUnitButton = (Button) findViewById(R.id.add);
        addUnitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                add();                
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_unit, menu);
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

    public void add() {
        Intent intent = new Intent(this, AllUnitsActivity.class);
        EditText et = (EditText) findViewById(R.id.addUnitEditText);
        String unitName = et.getText().toString();
        long id = mHardwareUnitsDataSource.addHardwareUnit(unitName);
        intent.putExtra(EXTRA_UNIT_ID, id);  // pass back unit id
        startActivity(intent);
    }
}
