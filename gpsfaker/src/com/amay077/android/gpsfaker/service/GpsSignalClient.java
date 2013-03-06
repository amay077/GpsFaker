package com.amay077.android.gpsfaker.service;

import hu.akarnokd.reactive4java.base.Func1;
import android.content.Context;
import android.os.IBinder;

import com.amay077.android.exception.SystemException;
import com.amay077.android.service.BaseServiceClient;

public class GpsSignalClient extends BaseServiceClient<IGpsSignalApi>
	implements IGpsSignalApi {

	public GpsSignalClient(Context context) {
		super(context, GpsSignalService.class, 
				new Func1<IBinder, IGpsSignalApi>() {
			@Override
			public IGpsSignalApi invoke(IBinder binder) {
				return IGpsSignalApi.Stub.asInterface(binder);
			}
		});
	}

	@Override
	public void setGpxPath(String path) {
		try {
			getApi().setGpxPath(path);
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	public String getGpxPath() {
		try {
			return getApi().getGpxPath();
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	public void start() {
		try {
			getApi().start();
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	public void stop() {
		try {
			getApi().stop();
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	public void pause() {
		try {
			getApi().pause();
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	public boolean isActive() {
		try {
			return getApi().isActive();
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	protected void onStopService() {
	}

	@Override
	protected boolean canStopService() {
		try {
			return !getApi().isActive();
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	public int getPlayStatus() {
		try {
			return getApi().getPlayStatus();
		} catch (Exception e) {
			return 0;
		}
	}
}
