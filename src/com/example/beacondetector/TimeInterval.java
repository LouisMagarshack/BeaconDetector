package com.example.beacondetector;

import org.joda.time.DateTime;

public class TimeInterval {
	//We could also specify days of the week in this class.
	
	private int mBegin;
	private int mEnd;
	
	public TimeInterval(String begin, String end) {
		//TODO check that the parsing goes well.
		mBegin = DateTime.parse(begin).getMillisOfDay();
		mEnd = DateTime.parse(end).getMillisOfDay();
	}
	
	/**
	 * 
	 * @param time - time to test
	 * @return 0 if in the interval.<br>
	 * A negative number representing the number of milis before interval.<br>
	 * A positive number representing the number of milis after the interval.
	 * @throws TimeIntervalException If the given time is null.
	 */
	public int inInterval(DateTime time) throws TimeIntervalException {
		if (time == null) {
			throw new TimeIntervalException("TimeInterval was given a null time");
		}
		
		int timeInMilis = time.getMillisOfDay();
		
		if (timeInMilis < mBegin) {
			//Return a negative # when before the interval
			return timeInMilis - mBegin;
		} else if (mBegin <= timeInMilis && timeInMilis < mEnd) {
			//Return 0 when in the interval
			return 0;
		} else {
			//return positive # when after the interval
			return timeInMilis - mEnd;
		}
		
	}
	
	public int getBegin() {
		return mBegin;
	}
}
