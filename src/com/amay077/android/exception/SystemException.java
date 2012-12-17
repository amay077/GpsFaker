package com.amay077.android.exception;

public class SystemException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SystemException() {
	}

	public SystemException(String detailMessage) {
		super(detailMessage);
	}

	public SystemException(Throwable throwable) {
		super(throwable);
	}

	public SystemException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
