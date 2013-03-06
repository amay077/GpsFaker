package com.amay077.android.gpsfaker;

public enum PlayStatus {
	None(0),
	Play(1),
	Stop(2),
	Pause(3);
	
	private int _value;
	
	private PlayStatus(int value) {
		_value = value;
	}

	public static PlayStatus valueOf(int playStatus) {
		for (PlayStatus v : values()) {
			if (playStatus == v.intValue()) {
				return v;
			}
		}
		
		return None;
	}
	
	public int intValue() {
		return _value;
	}
}
