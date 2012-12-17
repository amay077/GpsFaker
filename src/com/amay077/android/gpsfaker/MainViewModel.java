package com.amay077.android.gpsfaker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.amay077.android.gpsfaker.service.GpsSignalClient;
import com.amay077.android.service.BaseServiceClient.ConnectionListener;
import com.amay077.lang.Command;
import com.amay077.lang.ObservableValue;

public class MainViewModel {
	private static final String TAG = "MainViewModel";
	
	GpsSignalClient _serviceClient;
	
	// Observables /////////////////////////
	/** 実行ステータス */
	public final ObservableValue<PlayStatus> playStatus = new ObservableValue<PlayStatus>();
	/** GPXファイルパス */
	public final ObservableValue<String> gpxPath = new ObservableValue<String>();
	public final ObservableValue<Location> gpsLocation = new ObservableValue<Location>();

	// Initializer //////////////////////////
	public void init(Context context) {
		
		_serviceClient = new GpsSignalClient(context);
		_serviceClient.startService(new ConnectionListener() {
			@Override
			public void onServiceConnected() {
				gpxPath.set(_serviceClient.getGpxPath());
				playStatus.set(PlayStatus.valueOf(_serviceClient.getPlayStatus()));
			}

			@Override
			public void onServiceDisconnected() {
			}
		});
	}
	
	// Commands /////////////////////////////
	
	// 開始コマンド
	public final Command playCommand = new Command() {
		@Override
		public void execute() {
			Log.d(TAG, "playCommand.execute called.");
			playStatus.set(PlayStatus.Play);
			_serviceClient.setGpxPath(gpxPath.get());
			_serviceClient.start();
		}
	};

	// 停止コマンド
	public final Command stopCommand = new Command() {
		@Override
		public void execute() {
			Log.d(TAG, "stopCommand.execute called.");
			playStatus.set(PlayStatus.Stop);
			_serviceClient.stop();
		}
	};

	// 一時停止コマンド
	public final Command pauseCommand = new Command() {
		@Override
		public void execute() {
			Log.d(TAG, "pauseCommand.execute called.");
			playStatus.set(PlayStatus.Pause);
			_serviceClient.pause();
		}
	};

	public void terminate() {
		_serviceClient.stopServiceIfDeactiveAndClose();
	}
}
