package com.amay077.android.gpsfaker;

import com.amay077.android.gpsfaker.R;
import com.amay077.lang.Command;
import com.amay077.lang.ObservableValue;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

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
		// コース
		bindSpinnerToClick(_viewModel.gpxPath, (Spinner)findViewById(R.id.CourseSpinner));
		
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
	private void bindSpinnerToClick(final ObservableValue<String> value, final Spinner spinner) {
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Spinner s = (Spinner) parent;
				String item = (String) s.getSelectedItem();
				value.set(item);
			}			

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
//		button.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				command.execute();
//			}
//		});
	}
	
	@Override
	protected void onDestroy() {
		_viewModel.ternimate();
		super.onDestroy();
	}
}