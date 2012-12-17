package com.amay077.android.mvvm;

import hu.akarnokd.reactive4java.base.Action1;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class Messenger {
	
	private Map<String, Action1<? extends Message>> _actions = 
			new HashMap<String, Action1<? extends Message>>();
	
	@SuppressWarnings("unchecked")
	public void send(Message message) {
		final String messengerTypeName = message.getClass().getName();
		
		if (!_actions.containsKey(messengerTypeName)) {
			return;
		}
		
		@SuppressWarnings("rawtypes")
		Action1 action1 = _actions.get(messengerTypeName);
		action1.invoke(message);
	}

	public <T extends Message> void register(Action1<T> action) {
		Type[] types = action.getClass().getGenericInterfaces();
		String typeString = types[0].toString();

		int start = typeString.indexOf("<");
		int end = typeString.lastIndexOf(">");
		
		String nameOfT = typeString.subSequence(start + 1, end).toString();
		
		_actions.put(nameOfT, action);
	}
}
