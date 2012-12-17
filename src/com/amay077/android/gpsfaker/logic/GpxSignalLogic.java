package com.amay077.android.gpsfaker.logic;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.location.Location;

import com.amay077.android.gpsfaker.PlayStatus;
import com.amay077.android.gpsfaker.model.GpxModel;
import com.amay077.android.util.Log;
import com.amay077.lang.ObservableValue;

public class GpxSignalLogic {
	private static final String TAG = "GpxSignalLogic";

	public final ObservableValue<PlayStatus> playStatus = new ObservableValue<PlayStatus>();

	private GpxModel _model = new GpxModel();
	private String _gpxPath = "example.gpx";
	private Closeable _gpsSignalCloser;
	private boolean _pause = false;
	private WeakReference<Context> _context;

	public GpxSignalLogic(Context context) {
		_context = new WeakReference<Context>(context);
	}
	
	public void setGpxPath(String path) {
		_gpxPath = path;
	}

	public String getGpxPath() {
		return _gpxPath;
	}

	public void start() {
		Log.i(TAG, "start");
		playStatus.set(PlayStatus.Play);
		
		if (_pause) {
			_pause = false;
			return;
		}
		
		forceClose(_gpsSignalCloser);
		_gpsSignalCloser = _model.getLocationAsObservable(
				_context.get().getApplicationContext(), _gpxPath, 
				new Func0<Boolean>() {
					@Override
					public Boolean invoke() {
						return _pause;
					}
				})
		.register(new Observer<Location>() {

			@Override
			public void next(Location value) {
				Log.d(TAG, "playCommand.next called. - " + value);
				try {
//					gpsLocation.set(value);
				} catch (Exception e) {
					Log.e(TAG, "playCommand.next failed.", e);
					error(e);
				}
			}

			@Override
			public void finish() {
				Log.d(TAG, "playCommand.finish called.");
				_gpsSignalCloser = null;
			}

			@Override
			public void error(Throwable err) {
				Log.e(TAG, "playCommand.error() called.", err);
				_gpsSignalCloser = null;
			}
		});
	}

	public void stop() {
		Log.i(TAG, "stop");
		forceClose(_gpsSignalCloser);
		_pause = false;
		playStatus.set(PlayStatus.Stop);
	}

	public void pause() {
		Log.i(TAG, "pause");
		_pause = true;
		playStatus.set(PlayStatus.Pause);
	}

	public boolean isActive() {
		return playStatus.get() == PlayStatus.Play;
	}
	
	private void forceClose(Closeable closer) {
		Log.d(TAG, "Closeable.close called.");
		try {
			if (closer != null) {
				closer.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "Closeable.close failed.", e);
		}
	}

	public int getPlayStatus() {
		return playStatus.get().ordinal();
	}
}
