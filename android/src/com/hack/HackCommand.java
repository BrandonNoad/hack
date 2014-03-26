package com.hack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;

public abstract class HackCommand {

	private ProgressDialog mProgressDialog;
	private HardwareUnit mHardwareUnit;
	private String mUrl;
	private Context mContext;
	private String mTest;

	public HackCommand(HardwareUnit unit, String url, Context context) {
		mHardwareUnit = unit;
		mUrl = url;
		mContext = context;
	}

	public String getUrl() {
		return mUrl;
	}

	public HardwareUnit getHardwareUnit() {
		return mHardwareUnit;
	}

	public Context getContext() {
		return mContext;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public void send() {
		showProgressDialog();
		HackConnectionManager m = ((HackApplication)mContext.getApplicationContext()).getConnectionManager();
		m.submitRequest(this);
	}

	public void onResponseReceived(JSONObject json) {
		hideProgressDialog();
	}

	public String getTest() {
		return mTest;
	}

	public void setTest(String s) {
		mTest = s;
	}

	public void returnEarly(String message) {
		JSONObject json = new JSONObject();

		try {
			json.put("error", message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		onResponseReceived(json);
	}


	// -- Private methods

	private void showProgressDialog() {
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setTitle("Sending Request...");
		mProgressDialog.setMessage("Please wait.");
		mProgressDialog.setCancelable(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}

	private void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
}
