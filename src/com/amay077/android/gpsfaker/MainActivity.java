package com.amay077.android.gpsfaker;

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
	private final Integer SIGNAL_INTERVAL_MS = 1000;
	private Spinner providerSpinner = null;
	private Intent serviceIntent = null;
	
	private GpsSignalServiceClient m_gpsService = null;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		serviceIntent = new Intent(IGpsSignalService.class.getName());
		
		providerSpinner = (Spinner) findViewById(R.id.ProviderSpinner);
		
		Button btn;

		// 再生
		btn = (Button) findViewById(R.id.ButtonPlay);
		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) {
			onPlayButtonClick(view); 
		} });

		// 一時停止
		btn = (Button) findViewById(R.id.ButtonPause);
		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) { onPauseButtonClick(view); } });

		// 停止
		btn = (Button) findViewById(R.id.ButtonStop);
		btn.setOnClickListener(new OnClickListener() { public void onClick(View view) { 
			onStopButtonClick(view); 
		} });

		bindService(serviceIntent, m_serviceConnection, Context.BIND_AUTO_CREATE);
	}

	protected void onProviderSelected(AdapterView<?> parent, View view, int position, long id) {
		if (m_gpsService == null) return;

		Spinner spn = (Spinner)parent;
		m_gpsService.setProvider((String)spn.getSelectedItem());
	}

	protected void onStopButtonClick(View view) {
		if (m_gpsService == null) return;
		m_gpsService.stop();
		m_gpsService.setProviderEnabled(false);
		stopService();
	}

	protected void onPauseButtonClick(View view) {
		if (m_gpsService == null) return;
		m_gpsService.pause();
		showToast("GPSログ再生を一時停止しました。Play で再開します。");
	}

	protected void onPlayButtonClick(View view) {
		if (m_gpsService == null) return;
		
		m_gpsService.setProvider(providerSpinner.getSelectedItem().toString());
		m_gpsService.setProviderEnabled(true);
		m_gpsService.setProviderStatus(LocationProvider.AVAILABLE);

		if (!m_gpsService.isInitialized()) {
			String sdCardDir = Environment.getExternalStorageDirectory().getPath();
			String gpsLogPath = sdCardDir + "/GpsFaker/gps.log";
			m_gpsService.init(gpsLogPath);
		}
		m_gpsService.setInterval(SIGNAL_INTERVAL_MS);
		m_gpsService.play();
		showToast("GPSログ再生を開始しました。");
	}

	protected void onGpsSignalServiceDisconnected(ComponentName className) {
		m_gpsService = null;
		showToast("サービスから切断しました。");
	}

	protected void onGpsSignalServiceConnected(ComponentName className, IBinder service) {
		m_gpsService = new GpsSignalServiceClient(IGpsSignalService.Stub.asInterface(service));
		showToast("サービスに接続しました。");
	}

	private void stopService() {
		if (m_gpsService == null) return;
		stopService(serviceIntent);
		showToast("サービスを停止しました。");
	}

	private void showToast(String mes) {
		Toast.makeText(MainActivity.this, mes, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(m_serviceConnection); // バインド解除
	}


}