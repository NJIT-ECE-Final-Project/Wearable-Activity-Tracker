package com.njit.ece.senior_project.medical_sensor.sensorstest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SensorsDisplay extends AppCompatActivity implements SensorEventListener {

    // keeps track of sensor values
    private SensorManager sensorManager;

    // history of accelerometer readings
    // Mapping from a long respreenting time
    // to a list of doubles representing [x, y, z, mag]
    private List<Pair<Long, Double>> accel_history = new ArrayList<>();
    // most recent readings (to plot)
    private DataBuffer<Pair<Long, Double>> accel_mag_recent = new DataBuffer<>(500);


    // keep track of the same, but for angular acceleration (gyroscope)

    // smoother to take time averages
    private DataSmootherHighPass smoother;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_display);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        smoother = new DataSmootherHighPass(20); // take 20 ms moving averages of data
    }


    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                // TODO: change this to get more frequent readings
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getLinearAcceleration(event);
            // min 8 m/s^2 max 20 m/s^2 on chart
            updateChart(0, 3);
        }
    }

    private void getLinearAcceleration(SensorEvent event) {
        float[] accel = event.values;
        double mag = Math.sqrt(accel[0]*accel[0] + accel[1]*accel[1] + accel[2]*accel[2]);

        Log.d("Sensors", "Updating sensor data");

        // update the displayed values
        ((TextView)this.findViewById(R.id.accel_x)).setText(Float.toString(accel[0]));
        ((TextView)this.findViewById(R.id.accel_y)).setText(Float.toString(accel[1]));
        ((TextView)this.findViewById(R.id.accel_z)).setText(Float.toString(accel[2]));
        ((TextView)this.findViewById(R.id.accel_mag)).setText(Double.toString(mag));


        // store results in history
        Double[] listVals = new Double[]{(double) accel[0], (double) accel[1], (double) accel[2], mag};
        accel_history.add(new Pair<>(event.timestamp, mag));


        //accel_mag_recent.add(new Pair(event.timestamp, mag));

        Pair<Long, Double> nextPair = smoother.getNextDataPoint(event.timestamp, mag);
        if(nextPair != null) {
            accel_mag_recent.add(nextPair);
        }
    }


    private void updateChart(int minY, int maxY) {

        Log.d("Chart", "Updating chart");

        // get the chart reference
        LineChart lineChart = (LineChart) this.findViewById(R.id.accel_graph);

        // add all of the data to the chart
        List<Entry> chartEntries = new ArrayList<>();
        for(Pair<Long, Double> entry : accel_mag_recent.getData()) {
            chartEntries.add(new Entry(entry.first, (float) (double) entry.second));
        }
        Log.d("Chart", "Added " + accel_mag_recent.getData().size() + " items to the chart");

        // sort the entries
        Collections.sort(chartEntries, new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry t1) {
                return entry.getX() > t1.getX() ? 1 : (entry.getX() == t1.getX() ? 0 : -1);
            }
        });

        // create data set
        LineDataSet accelData = new LineDataSet(chartEntries, "Accel Mag");
        LineData data = new LineData(accelData);
        data.setDrawValues(false);

        // add data to chart
        lineChart.setData(data);

        // set axis data
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        leftAxis.setAxisMinimum(minY);
        rightAxis.setAxisMinimum(minY);
        leftAxis.setAxisMaximum(maxY);
        rightAxis.setAxisMaximum(maxY);

        // refresh
        lineChart.invalidate();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // TODO: now sure what causes this to happen
        Log.w("Sensors", "Accuraccy changed on " + sensor.getName());
        Log.w("Sensors", "i = " + i);
    }
}
