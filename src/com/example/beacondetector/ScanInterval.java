package com.example.beacondetector;

import org.joda.time.DateTime;

import android.bluetooth.BluetoothDevice;

public class ScanInterval /*<T>*/ {
	private TimeInterval mInterval;
	private ScanFrequency mFrequency;
	private DeviceFoundCallback mCallback;
	
	//This can be used to remove intervals from the list.
	//It is important that it should not be respected.
	private String mPluginName;
	
	
	
	public ScanInterval(TimeInterval interval, ScanFrequency frequency,
			DeviceFoundCallback cb, String pluginName) {
		mInterval = interval;
		mFrequency = frequency;
		mCallback = cb;
		mPluginName = pluginName;
		
	}
	
	public long inInterval(DateTime time) throws TimeIntervalException {
		return mInterval.inInterval(time);
	}
	
	public long getBegin() {
		return mInterval.getBegin();
	}
	
	public long getScanDuration() {
		return mFrequency.getScanDuration();
	}
	
	public long getRestDuration() {
		return mFrequency.getRestDuration();
	}
	
	public void executeCallback(final BluetoothDevice device, int rssi,
			  byte[] scanRecord) {
		mCallback.execute(device, rssi, scanRecord);
	}
	
	public String getPluginName() {
		return mPluginName;
	}

}
