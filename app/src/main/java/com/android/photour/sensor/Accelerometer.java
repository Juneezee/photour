package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Accelerometer {

  private static final String TAG = Accelerometer.class.getSimpleName();
  private SensorEventListener mAccelerationListener = null;
  private SensorManager mSensorManager;
  private Sensor mAccelerometerSensor;
  private long timePhoneWasLastRebooted = 0;
  private long lastReportTime = 0;
  private Barometer barometer;
  private float lastX = 0;
  private float lastY = 0;
  private float lastZ = 0;


  public Accelerometer(Context context, Barometer barometer) {
    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    this.barometer = barometer;
    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    initAccelerometerListener();
  }

  /**
   * it inits the listener and establishes the actions to take when a reading is available
   */
  private void initAccelerometerListener() {
    if (standardAccelerometerAvailable()) {
      mAccelerationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          long actualTimeInMseconds =
              timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
          float x = event.values[0];
          float y = event.values[1];
          float z = event.values[2];

          float deltaX = Math.abs(lastX - event.values[0]);
          float deltaY = Math.abs(lastY - event.values[1]);
          float deltaZ = Math.abs(lastZ - event.values[2]);
          // if the change is below 2, it is just plain noise
          if (deltaX < 2) {
            deltaX = 0;
          }
          if (deltaY < 2) {
            deltaY = 0;
          }
          if (deltaZ < 2) {
            deltaZ = 0;
          }
          if (deltaX > 0 || deltaY > 0 || deltaZ > 0) {
            Log.i(TAG,
                mSecsToString(actualTimeInMseconds) + ": significant motion detected - x: " + deltaX
                    + ", y: " + deltaY + ", z:" + deltaZ);
            if (!barometer.isStarted()) {
              barometer.startSensingPressure(Accelerometer.this);
            }
            setLastReportTime(actualTimeInMseconds);
          }
          lastX = x;
          lastY = y;
          lastZ = z;
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
      };
    }
  }


  /**
   * it returns true if the sensor is available
   */
  public boolean standardAccelerometerAvailable() {
    return (mAccelerometerSensor != null);
  }

  /**
   * it starts the pressure monitoring
   */
  public void startAccelerometerRecording() {
    // if the sensor is null,then mSensorManager is null and we get a crash
    if (standardAccelerometerAvailable()) {
      // THE ACCELEROMETER receives as frequency a predefined subset of timing
      // https://developer.android.com/reference/android/hardware/SensorManager
      mSensorManager.registerListener(mAccelerationListener, mAccelerometerSensor,
          SensorManager.SENSOR_DELAY_UI);
    }
  }


  /**
   * this stops the barometer
   */
  public void stopAccelerometer() {
    if (standardAccelerometerAvailable()) {
      try {
        mSensorManager.unregisterListener(mAccelerationListener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }
    // remember to stop the barometer
    barometer.stopBarometer();
  }


  public long getLastReportTime() {
    return lastReportTime;
  }

  public void setLastReportTime(long lastReportTime) {
    this.lastReportTime = lastReportTime;
  }

  /**
   * it converts a number of mseconds since 1.1.1970 (epoch) to a current string date
   *
   * @param actualTimeInMseconds a time in msecs for the UTC time zone
   * @return a time string of type HH:mm:ss such as 23:12:54.
   */
  public static String mSecsToString(long actualTimeInMseconds) {
    Date date = new Date(actualTimeInMseconds);
    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    return (formatter.format(date));
  }
}
