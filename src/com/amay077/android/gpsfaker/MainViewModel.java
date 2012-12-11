package com.amay077.android.gpsfaker;

import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.amay077.lang.Command;
import com.amay077.lang.ObservableValue;

public class MainViewModel {
	private static final String TAG = "MainViewModel";
	
	// Observables /////////////////////////
	
	/**
	 * 実行ステータス
	 */
	public final ObservableValue<PlayStatus> playStatus = new ObservableValue<PlayStatus>();
	
	/**
	 * GPXファイルパス
	 */
	public final ObservableValue<String> gpxPath = new ObservableValue<String>();
	
	public final ObservableValue<Location> gpsLocation = new ObservableValue<Location>();

	// Models ///////////////////////////////
	private GpxModel _model = new GpxModel();

	// Closers //////////////////////////////
	private Closeable _gpsSignalCloser;

	private WeakReference<Context> _context;
	
	// Initializer //////////////////////////
	public void init(Context context) {
		_context = new WeakReference<Context>(context);
		String sdCardDir = Environment.getExternalStorageDirectory().getPath();
		gpxPath.set("20121211.gqlog");
		playStatus.set(PlayStatus.Stop);
	}
	
	// Commands /////////////////////////////
	
	// 開始コマンド
	public final Command playCommand = new Command() {
		@Override
		public void execute() {
			Log.d(TAG, "playCommand.execute called.");
			playStatus.set(PlayStatus.Play);
			
			forceClose(_gpsSignalCloser);
			
			_gpsSignalCloser = _model.getLocationAsObservable(
					_context.get().getApplicationContext(), gpxPath.get())
			.register(new Observer<Location>() {

				@Override
				public void next(Location value) {
					Log.d(TAG, "playCommand.next called. - " + value);
					try {
						gpsLocation.set(value);
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
	};

	// 停止コマンド
	public final Command stopCommand = new Command() {
		@Override
		public void execute() {
			Log.d(TAG, "stopCommand.execute called.");
			playStatus.set(PlayStatus.Stop);
			forceClose(_gpsSignalCloser);
		}
	};

	// 一時停止コマンド
	public final Command pauseCommand = new Command() {
		@Override
		public void execute() {
			Log.d(TAG, "pauseCommand.execute called.");
			playStatus.set(PlayStatus.Pause);
		}
	};
	
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

	public void ternimate() {
		forceClose(_gpsSignalCloser);
	}
}
