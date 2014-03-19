package com.hack;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

public class SetTimerActivity extends Activity
implements TimePickerDialog.OnTimeSetListener {
    
    // -- Constants
    
    public static final String EXTRA_TIME_ON = "com.hack.TIME_ON";
    public static final String EXTRA_TIME_OFF = "com.hack.TIME_OFF";
    public static final String EXTRA_IS_REPEATED = "com.hack.IS_REPEATED";
    
    // -- Member variables
    private TimerDataSource mTimerDataSource;
    private EditText mSelectedEditText;
    private EditText mTimeOnEditText;
    private EditText mTimeOffEditText;
    private CheckBox mIsRepeatedCheckBox;
    private long mDeviceId;

    // -- Initialize Activity
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);
        // Show the Up button in the action bar.
        setupActionBar();
        
        mTimerDataSource = new TimerDataSource(this);
        mTimerDataSource.open();
        
        // get device id from previous activity
        Intent intent = getIntent();
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        
        // set up event listeners
        mIsRepeatedCheckBox = (CheckBox) findViewById(R.id.repeatCheckBox);
        mTimeOnEditText = (EditText) findViewById(R.id.timeOnEditText);
        mTimeOffEditText = (EditText) findViewById(R.id.timeOffEditText);
        Button setTimerSaveButton = (Button) findViewById(R.id.setTimerSaveButton);
        
        mTimeOnEditText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSelectedEditText = (EditText) v;
                showTimePickerDialog();
                
            }
        });
        
        mTimeOffEditText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSelectedEditText = (EditText) v;
                showTimePickerDialog();
                
            }
        });
        
        setTimerSaveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addTimer();
                startDeviceDetailsActivity();
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
        getMenuInflater().inflate(R.menu.set_timer, menu);
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
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra(SingleUnitActivity.EXTRA_DEVICE_ID, mDeviceId);
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // -- Intents
    
    public void startDeviceDetailsActivity() {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(SingleUnitActivity.EXTRA_DEVICE_ID, mDeviceId);
        startActivity(intent);
    }
    
    // -- Time Picker Dialog
   
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String meridiem = "AM";
        
        if (hourOfDay >= 12) {
            meridiem  = "PM";
        }
        
        hourOfDay %= 12;
        
        if (hourOfDay == 0) {
            hourOfDay = 12;
        }
        
        String min = "" + minute;
        
        if (minute < 10) {
            min = "0" + minute;
        }
        
        String timeString = hourOfDay + ":" + min + " " + meridiem;
        
        if (mSelectedEditText != null) {
        mSelectedEditText.setText(timeString);
        mSelectedEditText = null;
        } else {
          Log.i("SetTimerActivity - onTimeSet()", "mSelectedEditText is null");  
        }
    }
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }   
    
    
    public static class TimePickerFragment extends DialogFragment {
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), (SetTimerActivity) getActivity(), hour, minute,
                                        false);
        }        
    }
    
// -- Model
    
    public void addTimer() {
        String timeOn = mTimeOnEditText.getText().toString();
        String timeOff = mTimeOffEditText.getText().toString();
        boolean isRepeated = mIsRepeatedCheckBox.isChecked();        
        long timerId = mTimerDataSource.addTimer(mDeviceId, timeOn, timeOff, isRepeated);
    }

}