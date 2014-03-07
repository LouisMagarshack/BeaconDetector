package com.example.beacondetector;

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
