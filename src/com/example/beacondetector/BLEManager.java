package com.example.beacondetector;

import org.joda.time.DateTime;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEManager {
	
	
	private final int SATURDAY = 6;
	
	private final int SUNDAY = 0;
	
	private final DateTime BEGIN_RUSH = new DateTime(2014, 02, 26, 11, 30);
	
	private final DateTime END_RUSH = new DateTime(2014, 02, 26, 14, 0);
	
	private final int SCAN_INTERVAL = 1000 /* 60 */ * 10; //10 minutes
	
	private final int SCAN_DURATION = 1000 * 100; //10 seconds
	
	private Handler mScanJobHandler;
	
	private Handler mScanHandler;
	
	private boolean mBLESupported = false;
	
	//TODO DEBUGGING, REMOVE
	private final MainActivity mUI;
	
	private final BluetoothManager mBluetoothManager;
	private final BluetoothAdapter mBluetoothAdapter;
	
	public BLEManager(Context ctx) {
		mUI = (MainActivity) ctx;
		
		mScanJobHandler = new Handler();
		mScanHandler = new Handler();
		mBluetoothManager = !bleSupported(ctx) ? null : 
			(BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager == null ? null : mBluetoothManager.getAdapter();
	}
	
	private boolean bleSupported(Context ctx) {
		mBLESupported =  true/*(ctx == null ? false : ctx.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE))*/; 
		
//		if (mBLESupported) {
//			mUI.showToast("BLE is supported.");
//		} else {
//			mUI.showToast("BLE is not supported.");
//		}
		return mBLESupported;
	}
	
	
	public void stopScanner() {
		try {
			mScanHandler.removeCallbacks(mScanner);
			mScanJobHandler.removeCallbacks(mScanScheduler);
		} catch (NullPointerException e) {
			//nothing special to do, it just means that mScanner was not initialized.
		}
	}
	
	public void startScanner() {
		if (mBLESupported) {			
			mScanScheduler.run();
		}
	}
	
	private void performScan() {
		if (!mBluetoothAdapter.isEnabled()) {
			
		}
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			
			return;
		}
		mScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }, SCAN_DURATION);

//        mScanning = true;
		
        mBluetoothAdapter.startLeScan(mLeScanCallback);
		
		
	}
	
	private boolean duringRushHour(DateTime time) {
		int hour = time.getHourOfDay();
		return hour >= BEGIN_RUSH.getHourOfDay() && hour < END_RUSH.getHourOfDay();
	}
	
	private boolean beforeRushHour(DateTime time) {
		int hour = time.getHourOfDay();
		int minutes = time.getMinuteOfHour();
		return hour < BEGIN_RUSH.getHourOfDay() ||
				(hour == BEGIN_RUSH.getHourOfDay() &&
						minutes < BEGIN_RUSH.getMinuteOfHour());
	}
	
	private boolean afterRushHour(DateTime time) {
		int hour = time.getHourOfDay();
		int minutes = time.getMinuteOfHour();
		return hour >= END_RUSH.getHourOfDay() ||
				(hour == END_RUSH.getHourOfDay() &&
					minutes >= END_RUSH.getMinuteOfHour());
	}
	
	
	private Runnable mScanScheduler = new Runnable() {
	    @Override 
	    public void run() {
	    	DateTime now = DateTime.now();
	    	int weekDay = now.getDayOfWeek();
	    	long nextInterval = SCAN_INTERVAL;
//	    	if (weekDay != SUNDAY) {
//	    		if (duringRushHour(now)) {
//	    			//We are during rush hour
//	    			performScan();
//	    		} else if (beforeRushHour(now)) {
//	    			nextInterval = Math.abs(BEGIN_RUSH.getMillisOfDay() - now.getMillisOfDay());
//
//	    		} else if (afterRushHour(now)) {
//	    			// set the date to the beginning of rush hour next day or in 2 days if it's saturday
//	    			DateTime future =  DateTime.now();
//	    			future = future.plusDays(future.getDayOfWeek() == SATURDAY ? 2 : 1);
//	    			future = future.withHourOfDay(BEGIN_RUSH.getHourOfDay());
//	    			future = future.withMinuteOfHour(BEGIN_RUSH.getMinuteOfHour());
//	    			future = future.withSecondOfMinute(BEGIN_RUSH.getSecondOfMinute());
//	    			
//	    			nextInterval = Math.abs(future.getMillis() - now.getMillis());
//	    		}
//	    	} else {
//	    		// set the date to the beginning of rush hour next day
//	    		
//	    		DateTime future =  DateTime.now();
//    			future = future.plusDays(1);
//    			future = future.withHourOfDay(BEGIN_RUSH.getHourOfDay());
//    			future = future.withMinuteOfHour(BEGIN_RUSH.getMinuteOfHour());
//    			future = future.withSecondOfMinute(BEGIN_RUSH.getSecondOfMinute());
//    			
//    			nextInterval = Math.abs(future.getMillis() - now.getMillis());
//	    	}
	    	
	    	
	    	//TODO this is for debugging
	    	
	    	performScan();
	    	mScanJobHandler.postDelayed(mScanScheduler, nextInterval);
	    }
	  };
	  
	  private Runnable mScanner;
	  
	  
      private BluetoothAdapter.LeScanCallback mLeScanCallback =
    		  new BluetoothAdapter.LeScanCallback() {
    	  @Override
    	  public void onLeScan(final BluetoothDevice device, int rssi,
    			  byte[] scanRecord) {
    		  mScanner = new Runnable() {
    	          @Override
    	          public void run() {
    	              Log.d("BLEManager DEBUG", "received name = " + device.getName());
    	              mUI.updateList(device);
    	          }
    	      };
    	      mScanner.run();
    	  }
      };


}
