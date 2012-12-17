package com.amay077.android.mvvm;

import com.amay077.lang.Command;

public abstract class DefaultCommand implements Command {

	@Override
	public abstract void execute();

	@Override
	public boolean canExecute() {
		return true;
	}

}
