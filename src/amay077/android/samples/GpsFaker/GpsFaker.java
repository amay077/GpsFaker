package amay077.android.samples.GpsFaker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class GpsFaker extends Activity {

	// fields -----------------------------------------------------------------
	private GpsSignalService m_gpsService = null;
	private Context m_context = this;
	private ServiceConnection m_serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_gpsService = ((GpsSignalService.GpsSignalBinder)service).getService();

			IntentFilter filter = new IntentFilter(GpsSignalService.CHANGE_LOCATION);
			registerReceiver(m_receiver, filter);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			m_gpsService = null;
		}
	};

	private BroadcastReceiver m_receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			double lat = intent.getDoubleExtra("Latitude", 0d);
			double lng = intent.getDoubleExtra("Longitude", 0d);
			double time = intent.getLongExtra("Time", 0);

			String mes = String.format("GPSの位置が変更されました。- lat:%f, long:%f, time:%d", lat, lng, time);

			showToast(mes);
*/
		}
	};

	// ctor -------------------------------------------------------------------

	// setter/getter ----------------------------------------------------------

	// overrides --------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // サービス開始
        Button btn = (Button)findViewById(R.id.ButtonStartService);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (m_gpsService != null) return;
				startService();
				showToast("サービスを開始しました。");
			}
		});

        // サービス停止
        btn = (Button)findViewById(R.id.ButtonStopService);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (m_gpsService == null) return;
				stopService();
				showToast("サービスを停止しました。");
			}
		});

        // ログファイル選択
        btn = (Button)findViewById(R.id.ButtonSelectGpsLog);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			   	if (m_gpsService == null) return;
				m_gpsService.init("/sdcard/gps.log");
				showToast("GPSログファイルを設定しました。");
			}
		});

        // 再生
        btn = (Button)findViewById(R.id.ButtonPlay);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			   	if (m_gpsService == null) return;
				m_gpsService.play();
				showToast("GPSログ再生を開始しました。");
			}
		});

        // 一時停止
        btn = (Button)findViewById(R.id.ButtonPause);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			   	if (m_gpsService == null) return;
				m_gpsService.pause();
				showToast("GPSログ再生を一時停止しました。Play で再開します。");
			}
		});

        // 停止
        btn = (Button)findViewById(R.id.ButtonStop);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			   	if (m_gpsService == null) return;
				m_gpsService.stop();
				showToast("GPSログ再生を停止しました。Play で最初から再生します。");
			}
		});

		// 既に起動しているかもしれないサービスに接続
        Intent intent = new Intent(m_context, GpsSignalService.class);
		bindService(intent, m_serviceConnection, 0);
    }

	// public methods ---------------------------------------------------------

	// private methods --------------------------------------------------------

    private void startService() {
		// サービスを開始
		Intent intent = new Intent(m_context, GpsSignalService.class);
		startService(intent);

		// サービスにバインド
		bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopService() {
    	if (m_gpsService == null) return;

		unbindService(m_serviceConnection); // バインド解除
		unregisterReceiver(m_receiver); // 登録解除
		m_gpsService.stopSelf();
    }

    private void showToast(String mes) {
    	Toast.makeText(GpsFaker.this, mes, Toast.LENGTH_SHORT).show();
    }

	// sub classes ------------------------------------------------------------

}