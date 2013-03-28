package com.amay077.android.gpsfaker;

import com.amay077.android.util.Log;

import android.app.Application;

public class MyApplication extends Application {
	private static final String TAG = "MyApplication";
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate called.");
		super.onCreate();
	}
	
	@Override
	public void onLowMemory() {
		Log.i(TAG, "onLowMemory called.");
		super.onLowMemory();
	}
	
	@Override
	public void onTerminate() {
		Log.i(TAG, "onTerminate called.");
		super.onTerminate();
	}
	
}
