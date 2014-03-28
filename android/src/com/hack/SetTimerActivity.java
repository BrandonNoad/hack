package com.hack;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
    private HardwareUnitDataSource mHardwareUnitDataSource;
    private DeviceDataSource mDeviceDataSource;
    private int mSelectedEditTextId;
    private EditText mTimeOnEditText;
    private Date mTimeOnDate;
    private EditText mTimeOffEditText;
    private Date mTimeOffDate;
    private CheckBox mIsRepeatedCheckBox;
    private long mDeviceId;
    private Device mDevice;
    private HardwareUnit mHardwareUnit;
    private HackConnectionManager mConnectionManager;

    // -- Initialize Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);
        // Show the Up button in the action bar.
        setupActionBar();

        mTimerDataSource = new TimerDataSource(this);
        mTimerDataSource.open();
        mDeviceDataSource = new DeviceDataSource(this);
        mDeviceDataSource.open();
        mHardwareUnitDataSource = new HardwareUnitDataSource(this);
        mHardwareUnitDataSource.open();

        // get device id from previous activity
        Intent intent = getIntent();
        mDeviceId = intent.getLongExtra(SingleUnitActivity.EXTRA_DEVICE_ID, -1);
        mDevice = mDeviceDataSource.getDeviceById(mDeviceId);
        mHardwareUnit = mHardwareUnitDataSource.getHardwareUnitById(mDevice.getHardwareUnitId());

        mConnectionManager = ((HackApplication) getApplicationContext()).getConnectionManager();

        // set up event listeners
        mIsRepeatedCheckBox = (CheckBox) findViewById(R.id.repeatCheckBox);
        mTimeOnEditText = (EditText) findViewById(R.id.timeOnEditText);
        mTimeOffEditText = (EditText) findViewById(R.id.timeOffEditText);
        Button setTimerSaveButton = (Button) findViewById(R.id.setTimerSaveButton);

        mTimeOnEditText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSelectedEditTextId = R.id.timeOnEditText;
                showTimePickerDialog();

            }
        });

        mTimeOffEditText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSelectedEditTextId = R.id.timeOffEditText;
                showTimePickerDialog();

            }
        });

        setTimerSaveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                //grab the text from the EditText object
                String  timeOffText = mTimeOffEditText.getText().toString();
                String  timeOnText = mTimeOnEditText.getText().toString();
                if (timeOnText.isEmpty() || timeOffText.isEmpty()) {  // one of the the fields is empty

                    //create an alert dialog
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SetTimerActivity.this);

                    //set the message to be displayed
                    alertDialog.setMessage(R.string.dialog_message);

                    //set the behaviour of the button
                    alertDialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    //display the alert dialog
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                    return;
                }
                addTimer();

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
            upIntent.putExtra(SingleUnitActivity.EXTRA_HARDWARE_UNIT_ID, mHardwareUnit.getId());
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -- Intents

    public void startDeviceDetailsActivity() {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(SingleUnitActivity.EXTRA_DEVICE_ID, mDeviceId);
        intent.putExtra(SingleUnitActivity.EXTRA_HARDWARE_UNIT_ID, mHardwareUnit.getId());
        startActivity(intent);
    }

    // -- Time Picker Dialog

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        Date d = c.getTime();
        String timePicked = df.format(d);
        
        if (mSelectedEditTextId != -1) {
            EditText et = (EditText) findViewById(mSelectedEditTextId);
            et.setText(timePicked);
        }
        
        if (mSelectedEditTextId == R.id.timeOnEditText) {  // time on?
            mTimeOnDate = d;
        } else if (mSelectedEditTextId == R.id.timeOffEditText) {  // time off?
            mTimeOffDate = d;
        }
        
        mSelectedEditTextId = -1;
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }   

    public static class TimePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use 12AM as the default value for the picker
            int hour = 0;
            int minute = 0;
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), (SetTimerActivity) getActivity(), hour, minute,
                    false);
        }        
    }

    // -- Model

    public void addTimer() {
        long timeOn = mTimeOnDate.getTime();  // time in milliseconds
        long timeOff = mTimeOffDate.getTime();  // time in milliseconds
        
        long duration = timeOff - timeOn;
        if (duration < 0) {
            duration += 86400000;  // milliseconds per day
        }
        
        long now = new Date().getTime();
        
        long timeFromNow = timeOn - now;
        if (timeFromNow < 0) {
            timeFromNow += 86400000;
        }
        
        // convert to seconds for Espruino
        duration /= 1000;
        timeFromNow /= 1000;
        
        int isRepeated = (mIsRepeatedCheckBox.isChecked()) ? 1 : 0;
        
        String url = "http://" + mHardwareUnit.getBasePath() + ":" + mHardwareUnit.getPortNumber() + 
                     "/hack/setTimer?socket=" + mDevice.getSocketId() +"&timeFromNow=" + timeFromNow +
                     "&duration=" + duration + "&isRepeated=" + isRepeated;
        HackCommand setTimerCommand = new HackCommand(SetTimerActivity.this, mHardwareUnit, url) {

            @Override
            public void doSuccess(JSONObject response) {
                super.doSuccess(response);
                boolean isRepeated = mIsRepeatedCheckBox.isChecked();
                long timerId = mTimerDataSource.addTimer(mDeviceId, mTimeOnDate.getTime(), mTimeOffDate.getTime(), isRepeated);
                startDeviceDetailsActivity();
            }
        };
        
        setTimerCommand.send();

    }

}
