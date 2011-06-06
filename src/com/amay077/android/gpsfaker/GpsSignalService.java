/**
 *
 */
package com.amay077.android.gpsfaker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author amay077
 */
public class GpsSignalService extends Service {

	private IGpsSignalService.Stub gpsSignalService = null;

	@Override
	public void onCreate() {
		super.onCreate();
		
		gpsSignalService = new GpsSignalServiceImpl(this.getApplicationContext());
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        if(IGpsSignalService.class.getName().equals(intent.getAction())){
            return gpsSignalService;
        }
        return null;
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
}
