package com.amay077.android.gpsfaker;

interface IGpsSignalService {

	boolean isInitialized();
	
	boolean init(String path);
	void unInit();
	
	int getInterval();
	void setInterval(int intervalMS);

	void play();
	void pause();
	void stop();
	
	void setProviderEnabled(boolean enabled);
	void setProviderStatus(int available);
	
	void setProvider(String provider);
	String getProvider();
}
