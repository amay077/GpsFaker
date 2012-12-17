package com.amay077.android.mvvm;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Option;
import android.os.Parcelable;

public class StartActivityMessage implements Message {

	private Class<?> _activityClass;
	private Option<Parcelable> _parcel = Option.none();
	private Option<Action1<Parcelable>> _callback = Option.none();
	
	public StartActivityMessage(Class<?> activityClass) {
		_activityClass = activityClass;
	}

	public StartActivityMessage(Class<?> activityClass,
			Parcelable parcelable) {
		
		_activityClass = activityClass;
		_parcel = Option.some(parcelable);
	}

	public StartActivityMessage(Class<?> activityClass,
			Parcelable parcelable, Action1<Parcelable> callback) {
		
		_activityClass = activityClass;
		_parcel = Option.some(parcelable);
		if (callback != null) {
			_callback = Option.some(callback);
		}
	}
	
	public Class<?> getActivityClass() {
		return _activityClass;
	}
	
	public boolean hasParcel() {
		return Option.isSome(_parcel);
	}

	public Parcelable getParcel() {
		return _parcel.value();
	}
	
	public boolean hasCallback() {
		return Option.isSome(_callback);
	}

	public Action1<Parcelable> getCallback() {
		return _callback.value();
	}

}
