package com.example.beacondetector;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	BLEManager mBLEManager = null;
	
//	private LeDeviceListAdapter mLeDeviceListAdapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//		    Toast.makeText(this, "BLE not supported : (", Toast.LENGTH_SHORT).show();
//		    finish();
//		}
		
		
		//This should not be in an Activity as it's lifecyle is not stable.
		//It should be put in a configuration wrapper such as a class extending
		//Application
		mBLEManager = new BLEManager(this);
		
		mBLEManager.startScanner();
		
		try {
			ScanInterval myInt = new ScanInterval(
					new TimeInterval("8:00:00", "12:00:00"),
					new ScanFrequency(1800000, 5000),
					new DeviceFoundCallback() {
						
						@Override
						public void execute(BluetoothDevice device, int rssi, byte[] scanRecord) {
							updateList(device);
						}
					});
			ArrayList<ScanInterval> list = new ArrayList<ScanInterval>();
			list.add(myInt);
			mBLEManager.insertIntervals(list);
			
		} catch (ScanFrequencyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
//	@Override
//	protected void onResume() {
//		super.onResume();
//		
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void updateList(BluetoothDevice device) {
		((TextView) findViewById(R.id.output)).setText("Name = " + device.getName());
//		mLeDeviceListAdapter.addDevice(device);
//        mLeDeviceListAdapter.notifyDataSetChanged();
	}
	
	public void showToast(String content) {
		Toast.makeText(this, content, Toast.LENGTH_LONG).show();
	}
	
	

}
