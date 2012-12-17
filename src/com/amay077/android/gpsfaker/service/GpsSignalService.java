package com.amay077.android.gpsfaker.service;

import com.amay077.android.gpsfaker.R;
import com.amay077.android.util.Log;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GpsSignalService extends Service {
	private final String TAG = "GpsSignalService";
	private GpsSignalApiBinder _binder;
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		_binder = new GpsSignalApiBinder(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand Received start id " + startId + ": "
				+ intent);
		// 明示的にサービスの起動、停止が決められる場合の返り値
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return _binder;
	}
	
	void showLoggingNotification() {
		Intent intent = new Intent();
		String tickerText = "開始しました";
		String contentTitle = getString(R.string.app_name);
		String contentText = "開始です";
		
		PendingIntent contentIntent = PendingIntent.getService(this, 0, intent, 0);
		Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		startForeground(R.string.app_name, notification);
	}

	void closeLoggingNotification() {
		stopForeground(true);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}


}
