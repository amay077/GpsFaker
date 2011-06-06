package com.amay077.android.gpsfaker;

import android.os.RemoteException;
import android.util.Log;

public class GpsSignalServiceClient {
	private static final String TAG = "GpsSignalServiceClient";
	private IGpsSignalService gpsSignalService = null;
	
	public GpsSignalServiceClient(IGpsSignalService gpsSignalServiceIf) {
		this.gpsSignalService = gpsSignalServiceIf;
	}

	public void unInit() {
		try {
			gpsSignalService.unInit();
		} catch (RemoteException e) {
			Log.e(TAG, "unInit() failed.", e);
		}
	}

	public void play() {
		try {
			gpsSignalService.play();
		} catch (RemoteException e) {
			Log.e(TAG, "play() failed.", e);
		}
	}

	public void pause() {
		try {
			gpsSignalService.pause();
		} catch (RemoteException e) {
			Log.e(TAG, "pause() failed.", e);
		}
	}

	public void stop() {
		try {
			gpsSignalService.stop();
		} catch (RemoteException e) {
			Log.e(TAG, "stop() failed.", e);
		}
	}

	public void setProviderEnabled(boolean enabled) {
		try {
			gpsSignalService.setProviderEnabled(enabled);
		} catch (RemoteException e) {
			Log.e(TAG, "setProviderEnabled() failed.", e);
		}
	}

	public void setProviderStatus(int available) {
		try {
			gpsSignalService.setProviderStatus(available);
		} catch (RemoteException e) {
			Log.e(TAG, "setProviderStatus() failed.", e);
		}
	}

	public void setProvider(String provider) {
		try {
			gpsSignalService.setProvider(provider);
		} catch (RemoteException e) {
			Log.e(TAG, "setProvider() failed.", e);
		}
	}

	public String getProvider() {
		try {
			return gpsSignalService.getProvider();
		} catch (RemoteException e) {
			Log.e(TAG, "getProvider() failed.", e);
			return null;
		}
	}
	
	public boolean isInitialized() {
		try {
			return gpsSignalService.isInitialized();
		} catch (RemoteException e) {
			Log.e(TAG, "isInitialized() failed.", e);
			return false;
		}
	}
	
	public void init(String gpsLogPath) {
		try {
			gpsSignalService.init(gpsLogPath);
		} catch (RemoteException e) {
			Log.e(TAG, "init() failed.", e);
		}
	}

	public int getInterval() {
		try {
			return gpsSignalService.getInterval();
		} catch (RemoteException e) {
			Log.e(TAG, "getInterval() failed.", e);
			return 0;
		}
	}

	public void setInterval(int intervalMS) {
		try {
			gpsSignalService.setInterval(intervalMS);
		} catch (RemoteException e) {
			Log.e(TAG, "setInterval() failed.", e);
		}
	}
}
