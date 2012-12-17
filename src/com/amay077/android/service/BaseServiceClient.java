package com.amay077.android.service;

import hu.akarnokd.reactive4java.base.Func1;

import java.io.Closeable;
import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;

import com.amay077.android.util.Log;

public abstract class BaseServiceClient<TApi extends IInterface> 
	implements Closeable {
    private static final String TAG = "BaseServiceClient";
	private TApi _api;
	private WeakReference<Context> _context;
	private Func1<IBinder, TApi> _apiBindFunc;
	private ServiceConnection _serviceConnection;
	private final Intent _serviceIntent;
	
	public IBinder asBinder() {
		return null;
	}

	public interface ConnectionListener {
		void onServiceConnected();
		void onServiceDisconnected();
	}
	
    public BaseServiceClient(Context context, Class<?> cls, 
    		Func1<IBinder, TApi> apiBindFunc) {
    	Log.i(TAG, "ctor called.");
    	_serviceIntent = new Intent(context, cls);
    	_context = new WeakReference<Context>(context);
    	_apiBindFunc = apiBindFunc;
	}
    
	protected TApi getApi() {
		return _api;
	}

	protected abstract void onStopService();
	protected abstract boolean canStopService();

	public void startService(final ConnectionListener listener) {
    	Log.i(TAG, "startService called.");
		Context context = _context.get();
		
		if (_serviceConnection == null) {
			_serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName name, IBinder service) {
			    	Log.i(TAG, "startService#onServiceConnected called.");
					_api = _apiBindFunc.invoke(service);
					if (listener != null) {
						listener.onServiceConnected();
					}
				}

				public void onServiceDisconnected(ComponentName name) {
			    	Log.i(TAG, "startService#onServiceDisconnected called.");
					_api = null;
					if (listener != null) {
						listener.onServiceDisconnected();
					}
				}
			};
		}
		
		context.startService(_serviceIntent);
		context.bindService(_serviceIntent, _serviceConnection, 0);
	}

	public void stopService() {
    	Log.i(TAG, "stopService called.");
    	
    	if (!canStopService()) {
    		return;
    	}
    	
		onStopService();
		
		Context context = _context.get();
        context.stopService(_serviceIntent);
	}
	
	public void close() {
    	Log.i(TAG, "close called.");
		if (_serviceConnection != null) {
			_context.get().unbindService(_serviceConnection);
		}
	}
	
	public void stopServiceIfDeactiveAndClose() {
		if (canStopService()) {
			stopService();
		}
		
		close();
	}
}
