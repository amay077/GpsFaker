package com.amay077.android.gpsfaker;

import java.io.IOException;

import com.amay077.android.gpsfaker.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 
 * @author amay077
 */
public class MainActivity extends Activity {
	private Spinner providerSpinner = null;
	
	private GpsSignalService m_gpsService = null;
	private ServiceConnection m_serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			onGpsSignalServiceConnected(className, service);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			onGpsSignalServiceDisconnected(className);
		}
	};
	protected Integer signalIntervalMS = 1000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		providerSpinner = (Spinner) findViewById(R.id.ProviderSpinner);
		
//		final EditText edt = (EditText) findViewById(R.id.IntervalEdit);
//		edt.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (!hasFocus)
//					signalIntervalMS  = Integer.valueOf(edt.getText().toString());
//			}
//		});
		
		Button btn;
		
//		// プロバイダ有効化
//		btn = (Button) findViewById(R.id.ButtonEnableProvider);
//		btn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) { onProviderEnableButtonClick(view); }
//		});

//		// プロバイダのステータス変更 - AVAILABLE
//		btn = (Button) findViewById(R.id.ButtonAvailable); // サービス有効
//		btn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (m_gpsService == null)
//					return;
//				m_gpsService.setProviderStatus(LocationProvider.AVAILABLE);
//				showToast("ステータスを AVAILABLE にしました。");
//			}
//		});
//		// プロバイダのステータス変更 - OUT_OF_SERVICE
//		btn = (Button) findViewById(R.id.ButtonOutOfService); // サービス有効
//		btn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (m_gpsService == null)
//					return;
//				m_gpsService.setProviderStatus(LocationProvider.OUT_OF_SERVICE);
//				showToast("ステータスを OUT_OF_SERVICE にしました。");
//			}
//		});
//		// プロバイダのステータス変更 - TEMPORALY_UNAVAILABLE
//		btn = (Button) findViewById(R.id.ButtonTemporalyUnavailable); // サービス有効
//		btn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (m_gpsService == null)
//					return;
//				m_gpsService
//						.setProviderStatus(LocationProvider.TEMPORARILY_UNAVAILABLE);
//				showToast("ステータスを TEMPORARILY_UNAVAILABLE にしました。");
//			}
//		});

		// 再生
		btn = (Button) findViewById(R.id.ButtonPlay);
		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) {
			onProviderEnableButtonClick(view);
			
			if (m_gpsService == null)
				return;
			m_gpsService.setProviderStatus(LocationProvider.AVAILABLE);
//			showToast("ステータスを AVAILABLE にしました。");

			onPlayButtonClick(view); 
		} });

		// 一時停止
		btn = (Button) findViewById(R.id.ButtonPause);
		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) { onPauseButtonClick(view); } });

		// 停止
		btn = (Button) findViewById(R.id.ButtonStop);
		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) { 
			onStopButtonClick(view); 
			onProviderDisableButtonClick(view);
		} });

//		// プロバイダ無効化
//		btn = (Button) findViewById(R.id.ButtonDisableProvider);
//		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) { onProviderDisableButtonClick(view); } });

		// 既に起動しているかもしれないサービスに接続
		Intent intent = new Intent(this, GpsSignalService.class);
		bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);
	}

	protected void onProviderNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	protected void onProviderSelected(AdapterView<?> parent, View view, int position, long id) {
		if (m_gpsService == null) return;

		Spinner spn = (Spinner)parent;
		m_gpsService.setProvider((String)spn.getSelectedItem());
	}

	protected void onProviderDisableButtonClick(View view) {
		if (m_gpsService == null) return;
		m_gpsService.setProviderEnabled(false);
		stopService();
		showToast("プロバイダを無効にしました。");
	}

	protected void onStopButtonClick(View view) {
		if (m_gpsService == null) return;
		m_gpsService.stop();
		showToast("GPSログ再生を停止しました。Play で最初から再生します。");
	}

	protected void onPauseButtonClick(View view) {
		if (m_gpsService == null) return;
		m_gpsService.pause();
		showToast("GPSログ再生を一時停止しました。Play で再開します。");
	}

	protected void onPlayButtonClick(View view) {
		if (m_gpsService == null) return;
		if (!m_gpsService.isInitialized()) {
			String sdCardDir = Environment.getExternalStorageDirectory().getPath();
			String gpsLogPath = sdCardDir + "/GpsFaker/gps.log";
			try {
				m_gpsService.init(gpsLogPath);
			} catch (IOException e) {
				showToast(gpsLogPath + " が見つからないか開けません");
				return;
			}
		}
		m_gpsService.setIntervalMS(signalIntervalMS);
		m_gpsService.play();
		showToast("GPSログ再生を開始しました。");
	}

	protected void onGpsSignalServiceDisconnected(ComponentName className) {
		m_gpsService = null;
		showToast("サービスから切断しました。");
	}

	protected void onGpsSignalServiceConnected(ComponentName className, IBinder service) {
		m_gpsService = ((GpsSignalService.GpsSignalBinder)service).getService();
		showToast("サービスに接続しました。");
	}

	protected void onProviderEnableButtonClick(View view) {
		if (m_gpsService == null) {
			// サービスを開始
			Intent intent = new Intent(this, GpsSignalService.class);
			startService(intent);

			// サービスにバインド
			bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);
		} else {
			m_gpsService.setProvider(providerSpinner.getSelectedItem().toString());
			m_gpsService.setProviderEnabled(true);
		}
	}

	private void stopService() {
		if (m_gpsService == null) return;

		unbindService(m_serviceConnection); // バインド解除
		m_gpsService.stopSelf();
		showToast("サービスを停止しました。");
	}

	private void showToast(String mes) {
		Toast.makeText(MainActivity.this, mes, Toast.LENGTH_SHORT).show();
	}

	// sub classes ------------------------------------------------------------

}