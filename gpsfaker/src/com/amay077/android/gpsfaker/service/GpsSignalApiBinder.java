package com.amay077.android.gpsfaker.service;

import java.lang.ref.WeakReference;

import com.amay077.android.gpsfaker.logic.GpxSignalLogic;
import com.amay077.android.util.Log;

import android.os.RemoteException;

public class GpsSignalApiBinder extends IGpsSignalApi.Stub {
	private static final String TAG = "GpsSignalApiBinder";
	private GpxSignalLogic _logic;
	private WeakReference<GpsSignalService> _service;
	
	public GpsSignalApiBinder(GpsSignalService service) {
		_service = new WeakReference<GpsSignalService>(service);
		_logic = new GpxSignalLogic(service);
	}
	
	@Override
	public void setGpxPath(String path) throws RemoteException {
		_logic.setGpxPath(path);
	}

	@Override
	public String getGpxPath() throws RemoteException {
		return _logic.getGpxPath();
	}

	public void start() throws RemoteException {
		Log.i(TAG, "start called.");
		_service.get().showLoggingNotification();
		_logic.start();
	}

	public void stop() throws RemoteException {
		Log.i(TAG, "stop called.");
		_service.get().closeLoggingNotification();
		_logic.stop();
	}

	public void pause() throws RemoteException {
		Log.i(TAG, "pause called.");
		_logic.pause();
	}

	@Override
	public boolean isActive() throws RemoteException {
		return _logic.isActive();
	}
	
	@Override
	public int getPlayStatus() throws RemoteException {
		return _logic.getPlayStatus();
	}

}
