package com.example.beacondetector;

import android.bluetooth.BluetoothDevice;

public abstract class DeviceFoundCallback {

	public abstract void execute(final BluetoothDevice device, int rssi,
    			  byte[] scanRecord);
}
