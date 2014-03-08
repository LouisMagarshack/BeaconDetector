package com.example.beacondetector;

import org.joda.time.DateTime;

import android.bluetooth.BluetoothDevice;

public class ScanInterval /*<T>*/ {
	private TimeInterval mInterval;
	private ScanFrequency mFrequency;
	private DeviceFoundCallback mCallback;
	
	//This can be used to remove intervals from the list.
	//It is important that it should not be respected.
//	private String mPluginName;
	
	
	
	public ScanInterval(TimeInterval interval, ScanFrequency frequency,
			DeviceFoundCallback cb) {
		mInterval = interval;
		mFrequency = frequency;
		mCallback = cb;
//		mPluginName = pluginName;
		
	}
	
	public int inInterval(DateTime time) throws TimeIntervalException {
		return mInterval.inInterval(time);
	}
	
	public int getBegin() {
		return mInterval.getBegin();
	}
	
	public int getScanDuration() {
		return mFrequency.getScanDuration();
	}
	
	public int getRestDuration() {
		return mFrequency.getRestDuration();
	}
	
//	public void executeCallback(final BluetoothDevice device, int rssi,
//			  byte[] scanRecord) {
//		mCallback.execute(device, rssi, scanRecord);
//	}
	
	public DeviceFoundCallback getCallback() {
		return mCallback;
	}
	
//	public String getPluginName() {
//		return mPluginName;
//	}

}
