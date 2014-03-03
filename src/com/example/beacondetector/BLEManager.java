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
	/*
	 * before rush = [midnight; BEGIN_RUSH[
	 * during rush = [BEGIN_RUSH; END_RUSH[
	 * after rush = [END_RUSH; midnight[
	 * Sunday is a particular case: no rush on sundays
	 * 
	 */
	
	
	private final int SATURDAY = 6;
	
	private final int SUNDAY = 0;
	
	private final DateTime BEGIN_RUSH = new DateTime(2014, 02, 26, 11, 30);
	
	private final DateTime END_RUSH = new DateTime(2014, 02, 26, 14, 0);
	
	private final int SCAN_INTERVAL = 1000 /* 60 */ * 10; //10 minutes
	
	private final int SCAN_DURATION = 1000 * 10; //10 seconds
	
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
		mBLESupported =  (ctx == null ? false : ctx.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)); 
		
		if (mBLESupported) {
			mUI.showToast("BLE is supported.");
		} else {
			mUI.showToast("BLE is not supported.");
		}
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
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			//Should we ask the user to enable BT?
//			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			Log.d("BLEManager DEBUG", "BT is turned off");
			return;
		}
		mScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }, SCAN_DURATION);
		
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
	
	private long durationToNextRushMilis() throws SchedulerException {
		DateTime now = DateTime.now();
    	int weekDay = now.getDayOfWeek();
    	
    	//Next rush is monday
    	if (weekDay == SUNDAY) {
    		DateTime future =  DateTime.now();
			future = future.plusDays(1);
			future = future.withHourOfDay(BEGIN_RUSH.getHourOfDay());
			future = future.withMinuteOfHour(BEGIN_RUSH.getMinuteOfHour());
			future = future.withSecondOfMinute(BEGIN_RUSH.getSecondOfMinute());
			
			return Math.abs(future.getMillis() - now.getMillis());
    	}
    	
    	//Next rush in the same day
    	if (beforeRushHour(now)) {
    		return Math.abs(BEGIN_RUSH.getMillisOfDay() - now.getMillisOfDay());
    	}
    	
    	//During rush, fast schedule
    	if (duringRushHour(now)) {
    		return SCAN_INTERVAL;
    	}
    	
    	//Next rush is the next day or in 2 days
    	if (afterRushHour(now)) {
    		DateTime future =  DateTime.now();
			future = future.plusDays(future.getDayOfWeek() == SATURDAY ? 2 : 1);
			future = future.withHourOfDay(BEGIN_RUSH.getHourOfDay());
			future = future.withMinuteOfHour(BEGIN_RUSH.getMinuteOfHour());
			future = future.withSecondOfMinute(BEGIN_RUSH.getSecondOfMinute());
			
			return Math.abs(future.getMillis() - now.getMillis());
    	}
    	
    	//Should not happen if intervals are set correctly
    	throw new SchedulerException("Failed to find a time for the next scan attempt");
    	
	}
	
	private void scheduleNextScan() {
		
		
		try {
			mScanJobHandler.postDelayed(mScanScheduler, durationToNextRushMilis());
		} catch (SchedulerException e) {
			Log.d("BLEManager DEBUG", "the scheduler did not schedule" +
					" the next scan correctly. Set to default value instead (" + SCAN_INTERVAL + ")");
			mScanJobHandler.postDelayed(mScanScheduler, SCAN_INTERVAL);
		}
		
		
	}
	
	private Runnable mScanScheduler = new Runnable() {
	    @Override 
	    public void run() {
	    	DateTime now = DateTime.now();
	    	int weekDay = now.getDayOfWeek();
	    	
	    	if (weekDay != SUNDAY && duringRushHour(now)) {
	    		performScan();
	    	}
	    	
	    	scheduleNextScan();
	    	
	    	
	    	//TODO this is for debugging
//	    	performScan();
	    	
	    	
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

