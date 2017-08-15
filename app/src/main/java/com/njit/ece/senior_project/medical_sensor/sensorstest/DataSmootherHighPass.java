package com.njit.ece.senior_project.medical_sensor.sensorstest;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

/**
 * Smooths data sent to it by averaging over an interval and returning the average once
 * the interval is over
 *
 * Also performs a high pass on the data
 */
public class DataSmootherHighPass {

    private long intervalBegin;
    private long intervalEnd;
    private int intervalLength;

    private double currVal;
    private double prevData;

    private final double NANO_TO_MILLI = 1e-6;

    private final double ALPHA = 0.9;


    public DataSmootherHighPass(int length) {
        this.intervalLength = length;
    }

    @Nullable
    public Pair<Long, Double> getNextDataPoint(long currTime, double currData) {

        Pair<Long, Double> nextData = null;

        // convert time to milli seconds
        currTime = (long) (currTime * NANO_TO_MILLI);

        if(intervalBegin == 0) {
            // this is the first interval, so it begins at this time
            intervalBegin = currTime;
            intervalEnd = currTime + intervalLength;

            currVal = currData;
            prevData = currData;
            intervalBegin = intervalEnd;
            intervalEnd = intervalBegin + intervalLength;
        } else {

            // high-pass filter recurrence relation
            currVal = ALPHA*(currVal + currData - prevData);

            if (currTime > intervalEnd) {
                // time interval is ellpased, return the current data
                Log.d("DataSmootherHighPass", "Time: " + (long) ((intervalEnd + intervalBegin) / 2.0));
                nextData = new Pair<>((long) ((intervalEnd + intervalBegin) / 2.0), currVal);

                Log.d("DataSmootherHighPass", "Start: " + intervalBegin);
                Log.d("DataSmootherHighPass", "End: " + intervalEnd);
                Log.d("DataSmootherHighPass", "Length: " + (intervalEnd - intervalBegin));

                intervalBegin = intervalEnd;
                intervalEnd = intervalBegin + intervalLength;
            }
        }

        prevData = currData;

        return nextData;
    }

}
