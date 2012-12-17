package com.amay077.android.mvvm;

import android.content.Context;

import com.amay077.lang.ObservableValue;

public abstract class BaseViewModel {

	public final ObservableValue<String> toastMessage = new ObservableValue<String>();
	public final ObservableValue<String> indicatorMessege = new ObservableValue<String>();
	public final ObservableValue<Boolean> visibleIndicator = new ObservableValue<Boolean>();
	
	private final Messenger _messenger = new Messenger();
	
	protected abstract void onBindViewCompleted(Context context);

	protected BaseViewModel() {
	}
	
	public Messenger getMessenger() {
		return _messenger ;
	}
}
