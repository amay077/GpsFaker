package com.amay077.android.mvvm;

import android.os.Parcelable;

public class FinishActivityMessage implements Message {
	private Parcelable _okResult;
	
	public FinishActivityMessage() {
	}
	public FinishActivityMessage(Parcelable okResult) {
		_okResult = okResult;
	}
	
	public Parcelable getOkResult() {
		return _okResult;
	}
}
