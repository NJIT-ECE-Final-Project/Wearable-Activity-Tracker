package com.njit.ece.senior_project.medical_sensor.sensorstest;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A data buffer that keeps track of only the latest values that were put into it
 */
public class DataBuffer <T> {

    private int capacity = 0;
    private LinkedList<T> data;

    public DataBuffer(int capacity) {
        this.capacity = capacity;
        data = new LinkedList<>();
    }


    public synchronized void add(T d) {
        data.add(d);
        while(data.size() > capacity) {
            Log.d("DataBuffer", "Removing excess data");
            // remove oldest entries until the capacity is correct
            data.remove(0);
        }
    }

    public synchronized List<T> getData() {
        // create a copy because lists are mutable
        return new ArrayList<>(data);
    }

}
