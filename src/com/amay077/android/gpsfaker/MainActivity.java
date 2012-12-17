package com.amay077.android.gpsfaker;

import hu.akarnokd.reactive4java.base.Func1;

import com.amay077.android.gpsfaker.R;
import com.amay077.android.mvvm.BaseActivity;
import com.amay077.android.mvvm.BaseViewModel;
import com.amay077.lang.Command;
import com.amay077.lang.ObservableValue;
import com.amay077.lang.ObservableValue.OnValueChangedListener;

import android.app.Application;
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
public class MainActivity extends BaseActivity<Application> {
	private MainViewModel _viewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		bindViewModel(MainViewModel.class);
	}
	
	@Override
	protected void onBindViewModel(BaseViewModel vm) {
		_viewModel = (MainViewModel)vm;
		
		// Binding ////////////////////////////////////////////////////////////
		// 再生
		bindCommandToClick(_viewModel.playCommand, (Button)findViewById(R.id.ButtonPlay));
		bindObservableToEnable(_viewModel.playStatus, findViewById(R.id.ButtonPlay),
			new Func1<PlayStatus, Boolean>() {
				@Override
				public Boolean invoke(PlayStatus status) {
					return status != PlayStatus.Play;
				}
			});
		
		// 停止
		bindCommandToClick(_viewModel.stopCommand, (Button)findViewById(R.id.ButtonStop));
		bindObservableToEnable(_viewModel.playStatus, findViewById(R.id.ButtonStop),
			new Func1<PlayStatus, Boolean>() {
				@Override
				public Boolean invoke(PlayStatus status) {
					return !(status == PlayStatus.Stop || status == PlayStatus.None);
				}
			});

		// 一時停止
		bindCommandToClick(_viewModel.pauseCommand, (Button)findViewById(R.id.ButtonPause));
		bindObservableToEnable(_viewModel.playStatus, findViewById(R.id.ButtonPause),
			new Func1<PlayStatus, Boolean>() {
				@Override
				public Boolean invoke(PlayStatus status) {
					return !(status == PlayStatus.Stop || status == PlayStatus.None || status == PlayStatus.Pause);
				}
			});

		// コース
		bindSpinnerToClick(_viewModel.gpxPath, (Spinner)findViewById(R.id.CourseSpinner));
		
	}

	private <T> void bindObservableToEnable(ObservableValue<T> value,
			final View view, final Func1<T, Boolean> enableF) {
		
		value.addListener(new OnValueChangedListener<T>() {
			@Override
			public void onChanged(T newValue, T oldValue) {
				view.setEnabled(enableF.invoke(newValue));
			}
		});
	}

	private void bindCommandToClick(final Command command, final Button button) {
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				executeCommand(command);
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
		_viewModel.onDestroyView();
		super.onDestroy();
	}
}