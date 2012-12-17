package com.amay077.android.gpsfaker.service;

interface IGpsSignalApi {
	void setGpxPath(String path);
	String getGpxPath();

	void start();
	void stop();
	void pause();
	boolean isActive();
	int getPlayStatus();
}