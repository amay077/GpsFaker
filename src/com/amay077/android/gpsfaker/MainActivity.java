package com.amay077.android.gpsfaker;

import com.amay077.android.gpsfaker.R;
import com.amay077.lang.Command;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * @author amay077
 */
public class MainActivity extends Activity {
	private MainViewModel _viewModel = new MainViewModel();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Binding ////////////////////////////////////////////////////////////

		// 再生
		bindCommandToClick(_viewModel.playCommand, (Button)findViewById(R.id.ButtonPlay));
		// 停止
		bindCommandToClick(_viewModel.stopCommand, (Button)findViewById(R.id.ButtonStop));
		// 一時停止
		bindCommandToClick(_viewModel.pauseCommand, (Button)findViewById(R.id.ButtonPause));
		
		// Intialize ViewModel ////////////////////////////////////////////////
		
		_viewModel.init(this);
	}

	private void bindCommandToClick(final Command command, final Button button) {
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				command.execute();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		_viewModel.ternimate();
		super.onDestroy();
	}
}