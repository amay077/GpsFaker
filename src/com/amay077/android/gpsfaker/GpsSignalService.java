/**
 *
 */
package com.amay077.android.gpsfaker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import com.amay077.android.gpsfaker.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author h_okuyama
 * aaabbbb
 */
public class GpsSignalService extends Service {

/*	// public fields ----------------------------------------------------------
	public static final String ACTION = "Gps Faker Signal";
*/
	// fields -----------------------------------------------------------------
	public static final String CHANGE_LOCATION = "GpsFaker ChangeLocation";
	private final String PROVIDER_NAME = LocationManager.GPS_PROVIDER;

	private Timer m_timer = new Timer();
	private LocationManager m_locMan = null;
	private int m_interval = 1000;
	private BufferedReader m_gpsLogReader = null;

	// 通知領域
	private NotificationManager m_notificationManager = null;
	private Notification m_notification = null;

	// ctor -------------------------------------------------------------------
	// - none -

	// setter/getter ----------------------------------------------------------

	public boolean isInitialized() {
		return m_gpsLogReader != null;
	}

	public int getInterval() {
		return m_interval;
	}
	public void setInterval(int mInterval) {
		m_interval = mInterval;
	}

	// overrides --------------------------------------------------------------
	@Override
	public IBinder onBind(Intent arg0) {
		return new GpsSignalBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unInit();
	}

	// public methods ---------------------------------------------------------

	public boolean init(String path) {
		// GPSログ読み込み
		try {
			if (m_gpsLogReader != null) {
				m_gpsLogReader.close();
			}

			m_gpsLogReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

			// 通知領域の初期化
			initNotify();

		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void unInit() {

		try {
			stop();

			if (m_gpsLogReader != null) {
				m_gpsLogReader.close();
				m_gpsLogReader = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void play() {
		if (m_locMan == null) return;

		stopTimer();

		// 通知開始
		startNotify();

		m_timer = new Timer(false);
		TimerTask timerTask = new GpsSignalTask(m_locMan, m_gpsLogReader);
		m_timer.schedule(timerTask, 0, m_interval);
	}

	public void pause() {
		if (m_locMan == null) return;

		stopTimer();
	}

	public void stop() {
		if (m_locMan == null) return;

		stopTimer();

		if (m_gpsLogReader != null) {
			try {
				m_gpsLogReader.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 通知停止
		stopNotify();
	}

	// private methods --------------------------------------------------------
	private void stopTimer() {
		if (m_timer != null) {
			m_timer.cancel();
			m_timer.purge();
		}
	}

	/***
	 * 通知のためのオブジェクトの初期化を行う
	 */
	private void initNotify() {
		m_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		if (m_notificationManager == null) {
			return;
		}

		m_notification = new Notification(
				R.drawable.icon,
				"GPSログの再生を開始しました。",
				System.currentTimeMillis());

		// intentの設定
		PendingIntent contentIntent =
				PendingIntent.getActivity(this.getApplicationContext(), 0, null, 0);

		m_notification.setLatestEventInfo(
				getApplicationContext(),
				getText(R.string.app_name),
				"GPSログを再生しています。",
				contentIntent);

		// 「実行中」の領域に表示する
		m_notification.flags = Notification.FLAG_ONGOING_EVENT;
	}

	/***
	 * 通知領域への通知を開始する
	 */
	private void startNotify() {
		if (m_notificationManager != null) {
			m_notificationManager.notify(R.string.app_name, m_notification);
		}
	}

	/***
	 * 通知領域への通知を終了する
	 */
	private void stopNotify() {
		if (m_notificationManager != null) {
			m_notificationManager.cancel(R.string.app_name);
		}
	}

	// Sub Classes ------------------------------------------------------------
	class GpsSignalTask extends TimerTask {
		// fields -----------------------------------------------------------------
		private Location m_location = null;
		private LocationManager m_locMan = null;
		private BufferedReader m_gpsLogReader = null;

		// ctor -------------------------------------------------------------------
		public GpsSignalTask(LocationManager locMan, BufferedReader gpsLogReader) {
			super();

			this.m_locMan = locMan;
			this.m_gpsLogReader = gpsLogReader;
		}

		// setter/getter ----------------------------------------------------------
		// - none -

		// overrides --------------------------------------------------------------
		@Override
		public void run() {
			try {

				// ファイルから１行よむ
				String line = m_gpsLogReader.readLine();
				if (line == null) {
					return;
				}

				// カンマで分割（経度,緯度,高度）
				String[] buf = line.split(",");
				Location loc = new Location(PROVIDER_NAME);
				loc.setLongitude(Double.parseDouble(buf[0]));
				loc.setLatitude(Double.parseDouble(buf[1]));
				loc.setAltitude(Double.parseDouble(buf[2]));

				// 速度を計算して設定
				long curTime = System.currentTimeMillis();
				if (m_location != null) {
					float[] results = new float[3];
					Location.distanceBetween(this.m_location.getLatitude(), this.m_location.getLongitude(),
							loc.getLatitude(), loc.getLongitude(), results);
					float sec = ((float)(curTime - m_location.getTime()))/1000.0f;
	            	float meterPerSec = results[0] / sec;
	            	loc.setSpeed(meterPerSec);
				} else {
	            	loc.setSpeed(0);
				}
            	loc.setTime(curTime);

            	// GPSシグナル発火！
				this.m_location = loc;
				this.m_locMan.setTestProviderLocation(PROVIDER_NAME, this.m_location);

				Intent intent = new Intent(CHANGE_LOCATION);
				intent.putExtra("Latitude", this.m_location.getLatitude());
				intent.putExtra("Longitude", this.m_location.getLongitude());
				intent.putExtra("Altitude", this.m_location.getAltitude());
				intent.putExtra("Speed", this.m_location.getSpeed());
				intent.putExtra("Time", this.m_location.getTime());
				GpsSignalService.this.sendBroadcast(intent);

			} catch (IOException e) {

			} catch (NumberFormatException e) {

			} catch (Exception e) {

			}

		}

		// public methods ---------------------------------------------------------
		// - none -

		// private methods --------------------------------------------------------
		// - none -
	}

	class GpsSignalBinder extends Binder {

		GpsSignalService getService() {
			return GpsSignalService.this;
		}

	}

	public void setProviderEnabled(boolean enabled) {
		if (enabled && (m_locMan == null)) {
			m_locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

			try {
				Hoge hoge = new Hoge();
				Class c = this.m_locMan.getClass();
				Field[] flds = c.getDeclaredFields();
				for (int i = 0; i < flds.length; i++) {
					Log.d("enumFields", flds[i].getName());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			m_locMan.addTestProvider(PROVIDER_NAME,
	        		false, //requiresNetwork,
	        		true,  //requiresSatellite,
	        		false, //requiresCell,
	        		false, //hasMonetaryCost,
	        		true,  //supportsAltitude,
	        		true,  //supportsSpeed,
	        		false, //supportsBearing,
	        		0,     //powerRequirement,
	        		1);    //accuracy)
		}

		if (m_locMan != null) {
			// ここで onProviderEnabled/Disabled が出るはず
			m_locMan.setTestProviderEnabled(PROVIDER_NAME, enabled);
		}

		if (!enabled && (m_locMan != null)) {
			m_locMan.removeTestProvider(PROVIDER_NAME); // ←ここで本物のGPSも死んじゃうぽい
			m_locMan = null;
		}
	}

	public void setProviderStatus(int available) {
		if (m_locMan != null) {
			// ここで onStatusChanged(AVAILABLE etc) が出るはず
			m_locMan.setTestProviderStatus(PROVIDER_NAME,
					available, null, System.currentTimeMillis());
		}
	}

	class Hoge {
		private String addr = "hoge";
	}
}
