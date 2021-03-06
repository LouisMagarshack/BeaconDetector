package com.example.beacondetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
	
	//TODO Make this class a Singleton
	//Careful with concurrency!
	
	
	//TODO saturday is 6, this is for testing.
	private final int SATURDAY = 1;
	
	private final int SUNDAY = 0;
	
	//Need an ArrayList for random accesses.
	private ArrayList<ScanInterval> mIntervals;
	
//	private ScanInterval mCurrentInterval;
	
//	private final DateTime BEGIN_RUSH = new DateTime(2014, 02, 26, 11, 30);
//	
//	private final DateTime END_RUSH = new DateTime(2014, 02, 26, 14, 0);
	
//	private final int SCAN_INTERVAL = 1000 /* 60 */ * 10; //10 minutes
	
//	private final int SCAN_DURATION = 1000 * 10; //10 seconds
	
//	private Handler mScanJobHandler = new Handler();
	
	private Handler mScanHandler = new Handler();
	
	private boolean mBLESupported = false;
	
	//TODO DEBUGGING, REMOVE
//	private final MainActivity mUI;
	
	private final BluetoothManager mBluetoothManager;
	private final BluetoothAdapter mBluetoothAdapter;
	
	private boolean mScanning = false;
	
//	private BLEManager() {
//		mBluetoothManager = null;
//		mBluetoothAdapter = null;
//		
//		//TODO init with global ctx
////		mBluetoothManager = !bleSupported(ctx) ? null : 
////			(BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
////		
////		mBluetoothAdapter = mBluetoothManager == null ? null : mBluetoothManager.getAdapter();
//	}
	
//	private static class BLEManagerHolder {
//		private static final BLEManager INSTANCE = new BLEManager();
//	}
	
//	public static BLEManager getInstance() {
//		return BLEManagerHolder.INSTANCE;
//	}
	
	public BLEManager(Context ctx) {
		
		mBluetoothManager = !bleSupported(ctx) ? null : 
			(BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);

		mBluetoothAdapter = mBluetoothManager == null ? null : mBluetoothManager.getAdapter();
		
		mIntervals = new ArrayList<ScanInterval>();
		
	}
	
	private boolean bleSupported(Context ctx) {
		mBLESupported =  (ctx == null ? false : ctx.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)); 
		
//		if (mBLESupported) {
//			mUI.showToast("BLE is supported.");
//		} else {
//			mUI.showToast("BLE is not supported.");
//		}
		return mBLESupported;
	}
	
	
	public void stopScanner() {
		try {
			mScanHandler.removeCallbacks(mCallbackCaller);
			mScanning = false;
//			mScanJobHandler.removeCallbacks(mScanScheduler);
		} catch (NullPointerException e) {
			//nothing special to do, it just means that mScanner was not initialized.
		}
	}
	
	public void startScanner() {
		if (mBLESupported) {			
			mScanScheduler.run();
			mScanning = true;
		}
	}
	
	public void removeIntervals() {
//		Iterator<ScanInterval> iter = mIntervals.iterator();
//		while(iter.hasNext()) {
//			ScanInterval i = iter.next();
//			if (i.getPluginName().equals(pluginName)) {
//				iter.remove();
//			}
//		}
		mIntervals = new ArrayList<ScanInterval>();
		
		if (mScanning) {
			stopScanner();
			startScanner();
		}
	}
	
	public void insertIntervals(ArrayList<ScanInterval> newInter) {
		mIntervals.addAll(newInter);
		sortScans();

		if (mScanning) {
			stopScanner();
			startScanner();
		}
	}
	
	private void performScan(List<ScanInterval> inter) {
		Log.d("BLEManager DEBUG", "Call perform scan");
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			//Should we ask the user to enable BT?
//			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			Log.d("BLEManager DEBUG", "BT is turned off");
			return;
		}
		
		int max = 0;
		List<DeviceFoundCallback> callbacks = new LinkedList<DeviceFoundCallback>();
        for (ScanInterval i: inter) {
        	callbacks.add(i.getCallback());
        	if (i.getScanDuration() > max) {
        		max = i.getScanDuration();
        	}
        }
        
        Log.d("BLEManager DEBUG", "scanDuration = " + max);
        
        final LeScanMultCallbacks callback = new LeScanMultCallbacks(callbacks);
		
		mScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(callback);
                mScanScheduler.run();
            }
        }, max);
		
        mBluetoothAdapter.startLeScan(callback);
		
		
	}
	
//	private boolean duringRushHour(DateTime time) {
//		int hour = time.getHourOfDay();
//		return hour >= BEGIN_RUSH.getHourOfDay() && hour < END_RUSH.getHourOfDay();
//	}
//	
//	private boolean beforeRushHour(DateTime time) {
//		int hour = time.getHourOfDay();
//		int minutes = time.getMinuteOfHour();
//		return hour < BEGIN_RUSH.getHourOfDay() ||
//				(hour == BEGIN_RUSH.getHourOfDay() &&
//						minutes < BEGIN_RUSH.getMinuteOfHour());
//	}
//	
//	private boolean afterRushHour(DateTime time) {
//		int hour = time.getHourOfDay();
//		int minutes = time.getMinuteOfHour();
//		return hour >= END_RUSH.getHourOfDay() ||
//				(hour == END_RUSH.getHourOfDay() &&
//					minutes >= END_RUSH.getMinuteOfHour());
//	}
//	
//	private long durationToNextRushMilis() throws SchedulerException {
//		DateTime now = DateTime.now();
//    	int weekDay = now.getDayOfWeek();
//    	
//    	//Next rush is monday
//    	if (weekDay == SUNDAY) {
//    		DateTime future =  DateTime.now();
//			future = future.plusDays(1);
//			future = future.withHourOfDay(BEGIN_RUSH.getHourOfDay());
//			future = future.withMinuteOfHour(BEGIN_RUSH.getMinuteOfHour());
//			future = future.withSecondOfMinute(BEGIN_RUSH.getSecondOfMinute());
//			
//			return Math.abs(future.getMillis() - now.getMillis());
//    	}
//    	
//    	//Next rush in the same day
//    	if (beforeRushHour(now)) {
//    		return Math.abs(BEGIN_RUSH.getMillisOfDay() - now.getMillisOfDay());
//    	}
//    	
//    	//During rush, fast schedule
//    	if (duringRushHour(now)) {
//    		return SCAN_INTERVAL;
//    	}
//    	
//    	//Next rush is the next day or in 2 days
//    	if (afterRushHour(now)) {
//    		DateTime future =  DateTime.now();
//			future = future.plusDays(future.getDayOfWeek() == SATURDAY ? 2 : 1);
//			future = future.withHourOfDay(BEGIN_RUSH.getHourOfDay());
//			future = future.withMinuteOfHour(BEGIN_RUSH.getMinuteOfHour());
//			future = future.withSecondOfMinute(BEGIN_RUSH.getSecondOfMinute());
//			
//			return Math.abs(future.getMillis() - now.getMillis());
//    	}
//    	
//    	//Should not happen if intervals are set correctly
//    	throw new SchedulerException("Failed to find a time for the next scan attempt");
//    	
//	}
	
	private void sortScans() {
		Collections.sort(mIntervals, new Comparator<ScanInterval>() {

			@Override
			public int compare(ScanInterval lhs, ScanInterval rhs) {
				long lhsBegin = lhs.getBegin();
				long rhsBegin = rhs.getBegin();
				return lhsBegin < rhsBegin ? -1
						: lhsBegin > rhsBegin ? 1
						: 0;
			}
		});
	}
	
	private void scheduleNextScan() {
		if (mIntervals.size() == 0) {
			Log.d("BLEManager DEBUG", "Not scan Intervals");
			return;
		}
		
		DateTime now = DateTime.now();
		int weekDay = now.getDayOfWeek();
//		int nowMilis = now.getMillisOfDay();
		
		ArrayList<ScanInterval> subSet = new ArrayList<ScanInterval>();
		if (weekDay == SATURDAY || weekDay == SUNDAY) {
			Log.d("BLEManager DEBUG", "No scans on weekends");
			ScanInterval first = mIntervals.get(0);
			subSet.add(first);
			DateTime future = now.plusDays(weekDay == SATURDAY ? 2 : 1);
			future = future.withMillisOfDay(first.getBegin());
			mScanHandler.postDelayed(new RunnableScanner(subSet), (long)
					Math.abs(future.getMillis() - now.getMillis()));
			return;
		} else {
			int min = 60 * 60 * 1000;
			
			for(ScanInterval i : mIntervals) {
				int inInter;
				try {
					inInter = i.inInterval(now);
				} catch (TimeIntervalException e) {
					inInter = 1;
				}

				if (inInter == 0) {
					Log.d("BLEManager DEBUG", "Now in interval");
					if (i.getRestDuration() < min) {
						min = i.getRestDuration();
					}
					subSet.add(i);
				} else if (inInter > 0) {
					Log.d("BLEManager DEBUG", "Now in interval");
					if (subSet.size() == 0) {
						subSet.add(i);
						Log.d("BLEManager DEBUG", "rest = " 
						+ (long) Math.abs(i.getBegin() - now.getMillisOfDay()));
						mScanHandler.postDelayed(new RunnableScanner(subSet),
								(long) Math.abs(i.getBegin() - now.getMillisOfDay()));
						return;
					} else {
						Log.d("BLEManager DEBUG", "rest = " + min);
						mScanHandler.postDelayed(new RunnableScanner(subSet),
								min);
						return;
					}
				}
				if (subSet.size() != 0) {
					Log.d("BLEManager DEBUG", "rest = " + min);
					mScanHandler.postDelayed(new RunnableScanner(subSet),
							min);
					return;
				}
			}
		}

		
//		try {
//			mScanJobHandler.postDelayed(mScanScheduler, durationToNextRushMilis());
//		} catch (SchedulerException e) {
//			Log.d("BLEManager DEBUG", "the scheduler did not schedule" +
//					" the next scan correctly. Set to default value instead (" + SCAN_INTERVAL + ")");
//			mScanJobHandler.postDelayed(mScanScheduler, SCAN_INTERVAL);
//		}
		
		
	}
	
	private Runnable mScanScheduler = new Runnable() {
	    @Override 
	    public void run() {
//	    	DateTime now = DateTime.now();
//	    	int weekDay = now.getDayOfWeek();
//	    	
	    	
//	    	if (weekDay != SUNDAY && duringRushHour(now)) {
//	    		performScan();
//	    	}
	    	
	    	Log.d("BLEManager DEBUG", "Starting Scheduler");
	    	scheduleNextScan();
	    	
	    	
	    	//TODO this is for debugging
//	    	performScan();
	    	
	    	
	    }
	  };
	  
	  private class RunnableScanner implements Runnable {
		  
		  private List<ScanInterval> mScans;
		  
		  public RunnableScanner(List<ScanInterval> intervals) {
			  mScans = intervals;
		  }

		@Override
		public void run() {
			performScan(mScans);
		}
		  
	  }
	  
//	  private Runnable mScanner = new Runnable() {
//
//		  @Override
//		  public void run() {
//			  performScan();
//		  }
//	  };
	  
	  private Runnable mCallbackCaller;
	  
	  private class LeScanMultCallbacks implements BluetoothAdapter.LeScanCallback {
		  
		  private List<DeviceFoundCallback> mCallbacks;
		  
		  public LeScanMultCallbacks(List<DeviceFoundCallback> callbacks) {
			  mCallbacks = callbacks;
		  }

		@Override
		public void onLeScan(final BluetoothDevice device,
				final int rssi, final byte[] scanRecord) {
			mCallbackCaller = new Runnable() {
  	          @Override
  	          public void run() {
  	        	  //TODO send the received name to the server.
  	              Log.d("BLEManager DEBUG", "received name = " + device.getName());
//  	              mUI.updateList(device);
  	              for(DeviceFoundCallback cb: mCallbacks) {
  	            	  cb.execute(device, rssi, scanRecord);
  	              }
  	          }
  	      };
  	      mCallbackCaller.run();
			
		}
		  
	  }
	  
	  
      private BluetoothAdapter.LeScanCallback mLeScanCallback =
    		  new BluetoothAdapter.LeScanCallback() {
    	  
    	  
    	  
    	  @Override
    	  public void onLeScan(final BluetoothDevice device, int rssi,
    			  byte[] scanRecord) {
    		  
    	  }
      };


}

