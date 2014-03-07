package com.example.beacondetector;

public class ScanFrequency {
	
	private final static long HOUR_MS = 60 * 60 * 1000;
	
	private long mScanDuration;
	
	private long mRestDuration;
	
	public ScanFrequency(long upms, long scanDuration) throws ScanFrequencyException {
		if (upms <= 0 || upms > HOUR_MS ||
				scanDuration <= 0 || scanDuration > HOUR_MS ||
				scanDuration > upms) {
			throw new ScanFrequencyException("0 < upms <= 60 * 60 * 1000.\n" +
					"0 < scanDuration < upms");
		}
		
		mScanDuration = scanDuration;
		
		//Compute corresponding rest duration.
		double nbScans =  Math.ceil(((double) upms) / ((double) mScanDuration));
		
		double totalScanDuration = nbScans * ((double) mScanDuration);
		
		if (totalScanDuration > HOUR_MS) {
			nbScans -= 1.0;
		}
		
		if (nbScans <= 0.0) {
			nbScans = 1.0;
		}
		
		totalScanDuration = nbScans * ((double) mScanDuration);
		
		double nbRests = nbScans - 1.0;
		
		if (nbRests < 1.0) {
			mRestDuration = 0;
		} else {
			double totalRestDuration = ((double) HOUR_MS) - totalScanDuration;
			
			mRestDuration = (long) Math.floor(totalRestDuration / nbRests);

		}	
		
		
	}
	
	public long getScanDuration() {
		return mScanDuration;
	}
	
	public long getRestDuration() {
		return mRestDuration;
	}
}
