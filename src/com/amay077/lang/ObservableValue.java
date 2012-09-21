package com.amay077.lang;

import java.util.ArrayList;
import java.util.List;

public class ObservableValue<E> {
	public interface OnValueChangedListener<E> {
		public void onChanged(E newValue, E oldValue);
	}
	
	private E value;
	private List<OnValueChangedListener<E>> listeners;
	
	public ObservableValue() {
		this.listeners = new ArrayList<OnValueChangedListener<E>>();
	}

	public ObservableValue(E defaultValue) {
		this.listeners = new ArrayList<OnValueChangedListener<E>>();
		this.value = defaultValue;
	}
	
	public E get() {
		return value;
	}
	
	public void set(E value) {
		if (value == this.value || (value != null && value.equals(this.value))) return;
		
		E oldValue = this.value;
		this.value = value;
		for (OnValueChangedListener<E> l : listeners) {
			l.onChanged(value, oldValue);
		}
	}
	
	public void addListener(OnValueChangedListener<E> l) {
		listeners.add(l);
	}
	
	public void removeListener(OnValueChangedListener<E> l) {
		listeners.remove(l);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
}
