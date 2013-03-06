package com.amay077.android.gpsfaker.model;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.android.ddmuilib.location.GpxParser;
import com.android.ddmuilib.location.TrackPoint;
import com.android.ddmuilib.location.GpxParser.Track;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Functions;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class GpxModel {
	private static final String TAG = "GpxModel";
	private static final String PROVIDER_NAME = LocationManager.GPS_PROVIDER; //"GpsFaker";

	public Observable<Location> getLocationAsObservable(final Context context, final String logName,
			final Func0<Boolean> pauseF) {
		return Reactive.createWithCloseable(new Func1<Observer<? super Location>, Closeable>() {
			@Override
			public Closeable invoke(final Observer<? super Location> observer) {
				
				final AtomicBoolean stopped = new AtomicBoolean(false);
				final ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();
				
				final List<Location> testData;
				if (logName.endsWith(".gqlog")) {
					testData = makeGpsDataFromGqLog(context, logName);
				} else if (logName.endsWith(".gpx")) {
					testData = makeGpsDataFromGpx(context, logName);
				} else {
					observer.error(new UnsupportedOperationException(logName + " is invalid."));
					return Functions.EMPTY_CLOSEABLE;
				}
				
				final AtomicInteger index = new AtomicInteger(0);
				
				final LocationManager locMan = (LocationManager)context
						.getSystemService(Context.LOCATION_SERVICE);
				
				enableTestProvider(locMan);
				
				_executor.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						// 停止されていたら、値を無視する
						// ※停止要求した直後は意味が報告されることを想定しての処理。
						if (stopped.get()) {
							return;
						}
						
						if (pauseF.invoke()) {
							return;
						}
						
						Location location = testData.get(index.get());
						if (index.get() < testData.size() - 1) {
							index.addAndGet(1);
						} else {
							index.set(0);
						}
						
						location.setTime(System.currentTimeMillis());
						
						locMan.setTestProviderLocation(PROVIDER_NAME, location);
						observer.next(location);
					}
				}, 0, 1000, TimeUnit.MILLISECONDS);
				
				return new Closeable() {
					@Override
					public void close() throws IOException {
						if (stopped.get()) {
							return;
						}
						stopped.set(true);
						disableTestProvider(locMan);
						_executor.shutdownNow();
						observer.finish();
					}
				};
			}
		});
	}

	private List<Location> makeGpsDataFromGqLog(Context context, String gqLogName) throws NoSuchElementException {
		List<Location> locations = new ArrayList<Location>();
		
		AssetManager assetManager = context.getResources().getAssets();  
		InputStream input;
		try {
			input = assetManager.open(gqLogName);
		} catch (IOException e) {
			Log.e(TAG, "asset open failed.", e);
			return locations; 
		}  
		
		try {
			final long now = System.currentTimeMillis();
			Long prev = null;
			List<String> lines = readAsStringList(input, "UTF-8");
			for (String line : lines) {
				String[] cols = line.split(",");
				
				if (cols.length < 8) {
					continue;
				}
				
				long time = Long.valueOf(cols[0]) * 1000;
				Location location = new Location(PROVIDER_NAME);
				location.setLatitude(Double.valueOf(cols[1]));
				location.setLongitude(Double.valueOf(cols[2]));
				location.setAltitude(Double.valueOf(cols[3]));
				location.setAccuracy(Float.valueOf(cols[4]));

				if (prev == null) {
					location.setTime(now);
				} else {
					location.setTime(now + (time - prev));
				}
				prev = time;
				locations.add(location);
			}
		} catch (IOException e) {
			Log.w(TAG, "location parse failed.", e);
		}
	
		return locations;
	}
	
	private static List<String> readAsStringList(InputStream stream, String encoding) throws IOException {
		if (stream != null) {
			ArrayList<String> list = new ArrayList<String>();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
				String line;
				while (null != (line = reader.readLine())) {
					list.add(line);
				}
			} finally {
				stream.close();
			}
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	private List<Location> makeGpsDataFromGpx(Context context, String gpxPath) throws NoSuchElementException {
		List<Location> locations = new ArrayList<Location>();
		
		AssetManager assetManager = context.getResources().getAssets();  
		InputStream input;
		try {
			input = assetManager.open(gpxPath);
		} catch (IOException e) {
			Log.e(TAG, "asset open failed.", e);
			return locations; 
		}  
		
		GpxParser parser = new GpxParser(new InputStreamReader(input));
		
		if (!parser.parse()) {
			throw new NoSuchElementException("GpxParser.parse failed.");
		}
		Track[] tracks = parser.getTracks();
		if (tracks == null || tracks.length <= 0) {
			return locations;
		}
		
		final long now = System.currentTimeMillis();
		Long prev = null;
		for (TrackPoint point : tracks[0].getPoints()) {
			Location location = new Location(PROVIDER_NAME);
			location.setLatitude(point.getLatitude());
			location.setLongitude(point.getLongitude());
			location.setAccuracy(10f);
			location.setAltitude(point.getElevation());
			if (prev == null) {
				location.setTime(now);
			} else {
				location.setTime(now + (point.getTime() - prev));
			}
			prev = point.getTime();
// NOTE ignore			location.setTime(point.getTime());
			locations.add(location);
		}
		
		return locations;
	}
	
	public void enableTestProvider(LocationManager locMan) {
		locMan.addTestProvider(PROVIDER_NAME, 
				false, // requiresNetwork,
				true, // requiresSatellite,
				false, // requiresCell,
				false, // hasMonetaryCost,
				true, // supportsAltitude,
				true, // supportsSpeed,
				false, // supportsBearing,
				0, // powerRequirement,
				1); // accuracy)

		// ここで onProviderEnabled/Disabled が出るはず
		locMan.setTestProviderEnabled(PROVIDER_NAME, true);
	}
	
	public void disableTestProvider(LocationManager locMan) {
		locMan.setTestProviderEnabled(PROVIDER_NAME, false);
		locMan.removeTestProvider(PROVIDER_NAME); // ←ここで本物のGPSも死んじゃうぽい
	}
}