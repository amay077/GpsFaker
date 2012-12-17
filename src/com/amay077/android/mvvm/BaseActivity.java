package com.amay077.android.mvvm;

import hu.akarnokd.reactive4java.base.Action1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.amay077.android.util.Log;
import com.amay077.android.view.OnActivityResultCallback;
import com.amay077.lang.Command;
import com.amay077.lang.ObservableValue.OnValueChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * ベースアクティビティクラス
 *
 */
public abstract class BaseActivity<TApp extends Application> extends Activity {
	private static final String TAG = "BaseActivity";

	private static final int DIALOG_ID_PROGRESS = 999;

	/**
	 * 表示する Dialog を管理するマップ。
	 */
	private SparseArray<Dialog> _dialogMap = new SparseArray<Dialog>();

	private BaseViewModel _viewModel;

    /**
     * このアプリの Application クラスを取得する 
     */
    @SuppressWarnings("unchecked")
	public TApp getApp() {
    	return (TApp)this.getApplication();
    }

    /**
     * 戻り時の Action を指定して、画面遷移する 
     */
	public void startActivityWithResultAction(Context packageContext, Class<?> cls,
			OnActivityResultCallback resultAction) {
//		TApp app = getApp();
//		Integer requestCode = app.getActivityCallbackNum();
//		app.putActivityCallback(requestCode, resultAction);
//
//		Intent intent = new Intent(packageContext, cls);
//		startActivityForResult(intent, requestCode);
	}
	
    /**
     * パラメータを指定して画面遷移する 
     */
	public void startActivityWithParam(Context packageContext, Class<?> cls, 
			Parcelable param) {
		Intent intent = new Intent(packageContext, cls);
		intent.putExtra("startup_param", param);
		
		startActivity(intent);
	}

    /**
     * パラメータと、戻り時の Action を指定して、画面遷移する 
     */
	public void startActivityWithParamAndResultAction(Context packageContext, Class<?> cls,
			Parcelable param, OnActivityResultCallback resultAction) {
//		TApp app = getApp();
//		Integer requestCode = app.getActivityCallbackNum();
//		app.putActivityCallback(requestCode, resultAction);
//
//		Intent intent = new Intent(packageContext, cls);
//		if (param != null) {
//			intent.putExtra("startup_param", param);
//		}
//		startActivityForResult(intent, requestCode);
	}

	/**
	 * 呼び出し時のパラメータを取得する
	 */
	public Parcelable getStartupPrameter() {
		Intent intent = this.getIntent();
		if (intent == null) {
			return null;
		}
		
		if (!intent.hasExtra("startup_param")) {
			return null;
		}
		
		return intent.getParcelableExtra("startup_param"); 
	}
	
	/**
	 * 戻り時のパラメータを設定する。
	 */
	public void setOkResult(Parcelable result) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("activity_result", result);
		setResult(RESULT_OK, resultIntent);
	}

	/**
	 * Activity からの戻り時に、startActivityWithResultAction で渡された Action を呼び出す。
	 * 
	 * startActivityWithResultAction を実行した際、渡された Action をマップに追加します(Key は連番=requestCode)。
	 * その requestCode から Action を特定し、Invoke します。
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
//		TApp app = getApp();
//		OnActivityResultCallback callback = app.getActivityCallback(requestCode); 
//		if (resultCode == RESULT_OK && callback != null) {
//			Parcelable resultData = data.getParcelableExtra("activity_result");
//			callback.onResult(requestCode, resultCode, resultData);
//		}
//		else {
//			super.onActivityResult(requestCode, resultCode, data);
//		}
	}

	/**
	 * Dialog インスタンスを直接指定してダイアログを表示する。
	 * 
	 * Android のライフサイクルでは、showDialog(id) ⇢ onCreateDialog(id) 
	 * ⇢ dissmissDialog(id) の流れに沿わなければならないが、
	 * この方法では「ダイアログを表示させる処理の付近に、そのコールバックを書く」事ができず、
	 * 処理が分散してしまい見づらい。
	 * showDialogWithDialog(dialog) は直接 Dialog のインスタンスを指定して呼び出せる。
	 * 内部では、渡された dialog を hashCode をキーにして管理し、onCreateDialog にて呼び出している。
	 * 但し、setOnDismissListener は、このメソッドにより上書きされる。
	 */
	public void showDialogWithDialog(AlertDialog dialog) {
		int dialogId = dialog.hashCode();
		_dialogMap.put(dialogId, dialog);
		
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				removeDialog(dialog);
			}
		});
		
		showDialog(dialogId);
	}
	
	/**
	 * Dialog が生成される時
	 * 
	 * id が _dialogMap で管理されている値(hashcode)だったら、それを返す。
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_ID_PROGRESS) {
			return createProgressDialog();
		} else if (_dialogMap.indexOfKey(id) <= 0) {
			return _dialogMap.get(id);
		} else {
			return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == DIALOG_ID_PROGRESS) {
			ProgressDialog plg = (ProgressDialog)dialog;
			plg.setMessage(_viewModel.indicatorMessege.get());
		}
		super.onPrepareDialog(id, dialog);
	}
	
	/**
	 * Dialog を破棄し、キャッシュから削除する。
	 * 
	 * showDialogWithDialog(dlg) で表示させたダイアログは、必ずこのメソッドで破棄する必要がある。
	 */
	private void removeDialog(DialogInterface dialog) {
		int id = dialog.hashCode();
		if (_dialogMap.indexOfKey(id) <= 0) {
			removeDialog(id);
			_dialogMap.remove(id);
		}
	}

	
	private Dialog createProgressDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(_viewModel.indicatorMessege.get());
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setIndeterminate(true);
		return dialog;
	}
	
    protected void bindViewModel(Class<? extends BaseViewModel> classVm) {
    	Constructor<? extends BaseViewModel> ctor;
		Log.d(TAG, "bindViewModel called."); 
		try {
			ctor = classVm.getConstructor();
	    	_viewModel = ctor.newInstance();
	    	onBindViewModel(_viewModel);

	    	// 既定の Messenger を登録
	    	registerDefaultMessages();
	    	
	    	// 既定の Observable をバインド。
	    	bindDefaultObservables();
	    	
	    	_viewModel.onBindViewCompleted(this);
		} catch (SecurityException e) {
			Log.e(TAG, "bindViewModel security failed.", e); 
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "bindViewModel nosuch method failed.", e); 
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "bindViewModel illegal argument failed.", e); 
		} catch (InstantiationException e) {
			Log.e(TAG, "bindViewModel instantiation failed.", e); 
		} catch (IllegalAccessException e) {
			Log.e(TAG, "bindViewModel illegal access failed.", e); 
		} catch (InvocationTargetException e) {
			Log.e(TAG, "bindViewModel invocation target failed.", e); 
		}
	}
    
    private void bindDefaultObservables() {
    	// プログレスダイアログ
    	_viewModel.visibleIndicator.addListener(new OnValueChangedListener<Boolean>() {
			@Override
			public void onChanged(final Boolean newValue, final Boolean oldValue) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (newValue) {
							showDialog(DIALOG_ID_PROGRESS);
						} else {
							dismissDialog(DIALOG_ID_PROGRESS);
						}
					}
				});
			}
		});
    	
    	// Toast
    	_viewModel.toastMessage.addListener(new OnValueChangedListener<String>() {
			private Toast _toast;

			@Override
			public void onChanged(final String newValue, final String oldValue) {
				if (newValue == null || newValue.equals("")) {
					return;
				}
				
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (_toast != null) {
							_toast.cancel();
						}
						_toast = Toast.makeText(BaseActivity.this, newValue, Toast.LENGTH_SHORT);
						_toast.show();
						_viewModel.toastMessage.set(""); // すぐにクリア。
					}
				});
			}
		});
	}

	private void registerDefaultMessages() {
    	Messenger messenger = _viewModel.getMessenger();
    	
		// 画面遷移
		messenger.register(new Action1<StartActivityMessage>() {
			@Override
			public void invoke(final StartActivityMessage arg) {
				Parcelable parcel = null;
				if (arg.hasParcel()) {
					parcel = arg.getParcel();
				}
				
				startActivityWithParamAndResultAction(BaseActivity.this, 
						arg.getActivityClass(), parcel, new OnActivityResultCallback() {
					@Override
					public void onResult(int requestCode, int resultCode, Parcelable resultData) {
						if (arg.hasCallback()) {
							arg.getCallback().invoke(resultData);
						}
					}
				});
			}
		});
    	
    	// Activity 終了メッセージ
    	messenger.register(new Action1<FinishActivityMessage>() {
			@Override
			public void invoke(FinishActivityMessage arg) {
				BaseActivity<TApp> activity = BaseActivity.this;
				if (arg.getOkResult() != null) {
					activity.setOkResult(arg.getOkResult());
				}
				activity.finish();
			}
		});
	}

	protected AlertDialog createConfirmDialog(final String message, final String yesCaption, final String noCaption, 
    		final Action1<Boolean> resultCallback) {
    	return new AlertDialog.Builder(this)
		.setMessage(message)
		.setPositiveButton(yesCaption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				resultCallback.invoke(true);
			}
		})
		.setNegativeButton(noCaption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				resultCallback.invoke(false);
			}
		})
		.create();
	}

    /** Command を実行する共通関数 */
	protected void executeCommand(Command command) {
		if (!command.canExecute()) {
			Log.d(TAG, "command is not exectable - " + command.getClass().getName());
			return;
		}
		command.execute();
	}

	protected void setKeepScreenOn() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
	}
	protected void setKeepScreenOff() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

    // TODO abstract にしたいが、既存 Activity を修正するのが面倒なのであとで直す
    protected void onBindViewModel(BaseViewModel vm) {
    }
}
