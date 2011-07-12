package com.amay077.android.gpsfaker;

import com.android.ddmuilib.location.GpxParser.Track;
import com.android.ddmuilib.location.GpxParser;
import com.android.ddmuilib.location.TrackPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.RemoteException;

public class GpsSignalServiceImpl extends IGpsSignalService.Stub {
	public static final String CHANGE_LOCATION = "com.amay077.android.gpsfaker.LOCATION_CHANGED";
	private String providerName = LocationManager.GPS_PROVIDER;

//	private Timer m_timer = new Timer();
	private LocationManager m_locMan = null;
	private int intervalMS = 1000;
//	private BufferedReader m_gpsLogReader = null;
	private Track m_track = null;

	// 通知領域
	private NotificationManager m_notificationManager = null;
	private Notification m_notification = null;
	private Context context = null;
	private Thread mPlayingThread;
	private boolean mPlayingTrack;
	protected int mPlayDirection = 1;
	protected long mSpeed = 1;
	private Location m_location = null;


	public GpsSignalServiceImpl(Context context) {
		this.context = context;
	}

	@Override
	public boolean isInitialized() throws RemoteException {
//		return m_gpsLogReader != null;
		return m_track != null;
	}

	@Override
	public boolean init(String path) throws RemoteException {
		// GPSログ読み込み
		try {
			
			GpxParser parser = new GpxParser(path);
			if (!parser.parse()) {
				return false;
			}
			Track[] tracks = parser.getTracks();
			if (tracks == null || tracks.length <= 0) {
				return false;
			}
			
			m_track = tracks[0];
			
//			if (m_gpsLogReader != null)
//				m_gpsLogReader.close();
//
//			m_gpsLogReader = new BufferedReader(new InputStreamReader(
//					new FileInputStream(path)));

			// 通知領域の初期化
			initNotify();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void unInit() throws RemoteException {
		try {
			stop();

			m_track = null;
//			if (m_gpsLogReader != null) {
//				m_gpsLogReader.close();
//				m_gpsLogReader = null;
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getInterval() throws RemoteException {
		return intervalMS;
	}

	@Override
	public void setInterval(int intervalMS) throws RemoteException {
		this.intervalMS = intervalMS;
	}

	@Override
	public void play() throws RemoteException {
		if (m_locMan == null)
			return;

//		stopTimer();

		// 通知開始
		startNotify();

//		m_timer = new Timer(false);
//		TimerTask timerTask = new GpsSignalTask(m_locMan, m_gpsLogReader);
//		m_timer.schedule(timerTask, 0, intervalMS);
		playTrack(m_track);
	}

	@Override
	public void pause() throws RemoteException {
		if (m_locMan == null)
			return;
		
        mPlayingTrack = false;
        if (mPlayingThread != null) {
            mPlayingThread.interrupt();
        }
        
        //		stopTimer();
	}

	@Override
	public void stop() throws RemoteException {
		pause();

		// 通知停止
		stopNotify();
	}

	@Override
	public void setProviderEnabled(boolean enabled) throws RemoteException {
		if (enabled && (m_locMan == null)) {
			m_locMan = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			m_locMan.addTestProvider(providerName, false, // requiresNetwork,
					true, // requiresSatellite,
					false, // requiresCell,
					false, // hasMonetaryCost,
					true, // supportsAltitude,
					true, // supportsSpeed,
					false, // supportsBearing,
					0, // powerRequirement,
					1); // accuracy)
		}

		if (m_locMan != null) {
			// ここで onProviderEnabled/Disabled が出るはず
			m_locMan.setTestProviderEnabled(providerName, enabled);
		}

		if (!enabled && (m_locMan != null)) {
			m_locMan.removeTestProvider(providerName); // ←ここで本物のGPSも死んじゃうぽい
			m_locMan = null;
		}
	}

	@Override
	public void setProviderStatus(int available) throws RemoteException {
		if (m_locMan != null) {
			// ここで onStatusChanged(AVAILABLE etc) が出るはず
			m_locMan.setTestProviderStatus(providerName, available, null,
					System.currentTimeMillis());
		}
	}

	@Override
	public void setProvider(String provider) throws RemoteException {
		providerName = provider;
	}

	@Override
	public String getProvider() throws RemoteException {
		return providerName;
	}

	/***
	 * 通知のためのオブジェクトの初期化を行う
	 */
	private void initNotify() {
		m_notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (m_notificationManager == null) {
			return;
		}

		m_notification = new Notification(R.drawable.icon, "GPSログの再生を開始しました。",
				System.currentTimeMillis());

		Intent notifyIntent = new Intent(context, MainActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// intentの設定
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notifyIntent, 0);

		m_notification.setLatestEventInfo(context,
				context.getText(R.string.app_name), "GPSログを再生しています。",
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

//	private void stopTimer() {
//		if (m_timer != null) {
//			m_timer.cancel();
//			m_timer.purge();
//		}
//	}
//
//	// Sub Classes ------------------------------------------------------------
//	class GpsSignalTask extends TimerTask {
//		// fields
//		// -----------------------------------------------------------------
//		private Location m_location = null;
//		private LocationManager m_locMan = null;
//		private BufferedReader m_gpsLogReader = null;
//
//		// ctor
//		// -------------------------------------------------------------------
//		public GpsSignalTask(LocationManager locMan, BufferedReader gpsLogReader) {
//			super();
//
//			this.m_locMan = locMan;
//			this.m_gpsLogReader = gpsLogReader;
//		}
//
//		// setter/getter
//		// ----------------------------------------------------------
//		// - none -
//
//		// overrides
//		// --------------------------------------------------------------
//	}

	/**
	 * @param track
	 */
	private void playTrack(final Track track) {
		// no need to synchronize this check, the worst that can happen, is we
		// start the thread
		// for nothing.
		// if (mEmulatorConsole != null) {
		// mPlayGpxButton.setImage(mPauseImage);
		// mPlayKmlButton.setImage(mPauseImage);
		 mPlayingTrack = true;

		mPlayingThread = new Thread() {

			@Override
			public void run() {
				try {
					TrackPoint[] trackPoints = track.getPoints();
					int count = trackPoints.length;

					// get the start index.
					int start = 0;
					if (mPlayDirection == -1) {
						start = count - 1;
					}

					for (int p = start; p >= 0 && p < count; p += mPlayDirection) {
						if (mPlayingTrack == false) {
							return;
						}

						// get the current point and send its location to
						// the emulator.
						final TrackPoint trackPoint = trackPoints[p];

						synchronized (GpsSignalServiceImpl.this) {
							fireLocationChanged(
									trackPoint.getLatitude(),
									trackPoint.getLongitude(),
									trackPoint.getElevation());
						}

						// if this is not the final point, then get the next one
						// and
						// compute the delta time
						int nextIndex = p + mPlayDirection;
						if (nextIndex >= 0 && nextIndex < count) {
							TrackPoint nextPoint = trackPoints[nextIndex];

							long delta = nextPoint.getTime()
									- trackPoint.getTime();
							if (delta < 0) {
								delta = -delta;
							}

							long startTime = System.currentTimeMillis();

							try {
								sleep(delta / mSpeed);
							} catch (InterruptedException e) {
								if (mPlayingTrack == false) {
									return;
								}

								// we got interrupted, lets make sure we can
								// play
								do {
									long waited = System.currentTimeMillis()
											- startTime;
									long needToWait = delta / mSpeed;
									if (waited < needToWait) {
										try {
											sleep(needToWait - waited);
										} catch (InterruptedException e1) {
											// we'll just loop and wait again if
											// needed.
											// unless we're supposed to stop
											if (mPlayingTrack == false) {
												return;
											}
										}
									} else {
										break;
									}
								} while (true);
							}
						}
					}
				} finally {
					mPlayingTrack = false;
					// try {
					// mParent.getDisplay().asyncExec(new Runnable() {
					// public void run() {
					// if (mPlayGpxButton.isDisposed() == false) {
					// mPlayGpxButton.setImage(mPlayImage);
					// mPlayKmlButton.setImage(mPlayImage);
					// }
					// }
					// });
					// } catch (SWTException e) {
					// // we're quitting, just ignore
					// }
				}
			}
		};

		mPlayingThread.start();
	}
	
	private void fireLocationChanged(double latitude, double longitude, double altitude) {

		// カンマで分割（経度,緯度,高度）
		Location loc = new Location(providerName);
		loc.setLongitude(longitude);
		loc.setLatitude(latitude);
		loc.setAltitude(altitude);
		loc.setAccuracy(3f);

		// 速度を計算して設定
		long curTime = System.currentTimeMillis();
		if (m_location != null) {
			float[] results = new float[3];
			Location.distanceBetween(this.m_location.getLatitude(),
					this.m_location.getLongitude(), loc.getLatitude(),
					loc.getLongitude(), results);
			float sec = ((float) (curTime - m_location.getTime())) / 1000.0f;
			float meterPerSec = results[0] / sec;
			loc.setSpeed(meterPerSec);
		} else {
			loc.setSpeed(0);
		}
		loc.setTime(curTime);

		// GPSシグナル発火！
		this.m_location = loc;
		this.m_locMan
				.setTestProviderLocation(providerName, this.m_location);

		Intent intent = new Intent(CHANGE_LOCATION);
		intent.putExtra("Latitude", this.m_location.getLatitude());
		intent.putExtra("Longitude", this.m_location.getLongitude());
		intent.putExtra("Altitude", this.m_location.getAltitude());
		intent.putExtra("Speed", this.m_location.getSpeed());
		intent.putExtra("Time", this.m_location.getTime());
		context.sendBroadcast(intent);
	}

}
