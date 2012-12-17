package com.amay077.android.view;

import android.os.Parcelable;

/**
 * 遷移先の Activity から戻ってきた時に呼び出される Callback インターフェース。  
 * 
 * @author okuyama
 */
public interface OnActivityResultCallback {
	/**
	 * Activity からの戻り時に呼び出されるコールバック。
	 * 基本 onActivityresult のラッパだが、第３引数は Intent でなくパラメータそのもの。
	 */
	void onResult(int requestCode, int resultCode, Parcelable resultData);
}
