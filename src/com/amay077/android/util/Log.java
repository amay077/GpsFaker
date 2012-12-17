package com.amay077.android.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.format.PatternFormatter;

public class Log {
	static final private boolean _enabled = true;

	static final private Logger _logger = LoggerFactory.getLogger();
	static private boolean _isInitialized = false;

	static public void initialize(String appName) {
		if (!_enabled) return;
		if (_isInitialized) return;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		final String LOGFILE_PATH = appName + "/log/log" + dateFormat.format(new Date(System.currentTimeMillis())) + ".log";

		File sdCardDir = Environment.getExternalStorageDirectory();
		Uri logUri = Uri.withAppendedPath(Uri.fromFile(sdCardDir), LOGFILE_PATH);
		String logFullPath = logUri.getPath();

		File logDir = new File(logFullPath).getParentFile();
		if (!logDir.exists()) {
			logDir.mkdirs();
		}

		// Formatter
		PatternFormatter formatter = new PatternFormatter();
		formatter.setPattern("%d{ISO8601} [%P] %i:%m %T");

		// LogCatAppender
		LogCatAppender logCatAppender = new LogCatAppender();
		logCatAppender.setFormatter(formatter);
		_logger.addAppender(logCatAppender);

		// FileAppender
		FileAppender fileAppender = new FileAppender();
		fileAppender.setFileName(LOGFILE_PATH);
		fileAppender.setAppend(true);
		fileAppender.setFormatter(formatter);
//		_logger.addAppender(fileAppender);

		_isInitialized = true;
	}

	static public void writeApplicationInfo(Context context) {
		if (!_enabled) {
			return;
		}

		try {
			PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);

			_logger.info("PackageName:" + pkgInfo.packageName);
			_logger.info("VersionName:" + pkgInfo.versionName);
			_logger.info("VersionCode:" + String.valueOf(pkgInfo.versionCode));
			_logger.info("ApplicationName:" + pkgInfo.applicationInfo.name);
			_logger.info("IsDebuggable:" + String.valueOf(isDebug(context)));

		} catch (NameNotFoundException e) {
			_logger.warn("writeApplicationInfo failed.", e);
		}
	}

	public static boolean isDebug( Context context ) {
	    PackageManager pm = context.getPackageManager();
	    ApplicationInfo ai = new ApplicationInfo();
	    try {
	        ai = pm.getApplicationInfo( context.getPackageName(), 0 );
	    } catch( NameNotFoundException e ) {
	        ai = null;
	        return false;
	    }
	    if( (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE ) {
	        return true;
	    }
	    return false;
	}
	
	private enum Level { i, d, v, w, e }
	
	private static int write(Level level, String tag, String msg, Throwable tr) {
		if (!_enabled) { return 0; }
    	try {
    		_logger.setClientID(tag);
            
            switch (level) {
			case i: // INFO
				if (tr == null) { _logger.info(msg); 
				} else {		  _logger.info(msg, tr); }
				break;
			case d: // DEBUG
				if (tr == null) { _logger.debug(msg); 
				} else {		  _logger.debug(msg, tr); }
				break;
			case v: // VERBOSE -> trace
				if (tr == null) { _logger.trace(msg); 
				} else {		  _logger.trace(msg, tr); }
				break;
			case w: // WARNING
				if (tr == null) { _logger.warn(msg); 
				} else {		  _logger.warn(msg, tr); }
				break;
			case e: // ERROR
				if (tr == null) { _logger.error(msg); 
				} else {		  _logger.error(msg, tr); }
				break;
			default:
				break;
			}
            
            return 0;
		} catch (Exception e) {
			android.util.Log.e("Log", "write failed. - " + level + ", " + tag + ", " + msg, e);
			return 0;
		}
	}
	
    public static int d(String tag, String msg) {
    	return write(Level.d, tag, msg, null);
    }

    public static int d(String tag, String msg, Throwable tr) {
    	return write(Level.d, tag, msg, tr);
    }

    public static int i(String tag, String msg) {
    	return write(Level.i, tag, msg, null);
    }

    public static int i(String tag, String msg, Throwable tr) {
    	return write(Level.i, tag, msg, tr);
    }

    public static int w(String tag, String msg) {
    	return write(Level.w, tag, msg, null);
    }

    public static int w(String tag, String msg, Throwable tr) {
    	return write(Level.w, tag, msg, tr);
    }

    public static int e(String tag, String msg) {
    	return write(Level.e, tag, msg, null);
    }

    public static int e(String tag, String msg, Throwable tr) {
    	return write(Level.e, tag, msg, tr);
    }

    public static int v(String tag, String msg) {
    	return write(Level.v, tag, msg, null);
    }

    public static int v(String tag, String msg, Throwable tr) {
    	return write(Level.v, tag, msg, tr);
    }
}
