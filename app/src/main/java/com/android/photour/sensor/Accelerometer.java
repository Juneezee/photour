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
import java.util.Locale;
import java.util.TimeZone;

/**
 * A class for monitoring the movement of the device
 *
 * @author Professor Fabio Ciravegna, Zer Jun Eng, Jia Hua Ng
 */
public class Accelerometer {

  private static final String TAG = Accelerometer.class.getSimpleName();

  private SensorEventListener accelerationListener = null;
  private SensorManager sensorManager;
  private Sensor accelerometer;

  private Barometer barometer;
  private Ambient thermometer;

  private long timePhoneWasLastRebooted;
  private long lastReportTime = 0;
  private float lastX = 0;
  private float lastY = 0;
  private float lastZ = 0;

  /**
   * Constructor of the {@link Accelerometer} class
   *
   * @param context The context of the current application
   * @param barometer A {@link Barometer} instance
   */
  public Accelerometer(Context context, Barometer barometer) {
    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    this.barometer = barometer;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    initAccelerometerListener();
  }

  /**
   * Initialise the listener and establishes the actions to take when a reading is available
   */
  private void initAccelerometerListener() {
    if (standardAccelerometerAvailable()) {
      Log.d(TAG, "Using Accelerometer");
      accelerationListener = new SensorEventListener() {
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
            Log.d(TAG, mSecsToString(actualTimeInMseconds) + " : significant motion detected - x: "
                + deltaX + ", y: " + deltaY + ", z: " + deltaZ);
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
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };
    } else {
      Log.d(TAG, "Standard Accelerometer unavailable");
    }
  }

  /**
   * Check if accelerometer sensor is available on current device
   *
   * @return True if the accelerometer sensor is available on current device
   */
  public boolean standardAccelerometerAvailable() {
    return accelerometer != null;
  }

  /**
   * it starts the pressure monitoring
   */
  public void startAccelerometerRecording() {
    // if the sensor is null,then sensorManager is null and we get a crash
    if (standardAccelerometerAvailable()) {
      Log.d(TAG, "Starting accelerometer listener");
      // THE ACCELEROMETER receives as frequency a predefined subset of timing
      // https://developer.android.com/reference/android/hardware/SensorManager
      sensorManager.registerListener(accelerationListener, accelerometer,
          SensorManager.SENSOR_DELAY_UI);
    } else {
      Log.d(TAG, "Accelerometer unavailable or already active");
    }
  }

  /**
   * Stop the accelerometer, barometer, and thermometer
   */
  public void stopAccelerometer() {
    if (standardAccelerometerAvailable()) {
      Log.d(TAG, "Stopping accelerometer listener");
      try {
        sensorManager.unregisterListener(accelerationListener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }
    // remember to stop the barometer
    barometer.stopBarometer();
  }


  /**
   * Get the lastReportTime, the time where a sensor last reported its reading
   *
   * @return long The value of lastReportTime
   */
  public long getLastReportTime() {
    return lastReportTime;
  }

  /**
   * Set the new value of lastReportTime
   *
   * @param lastReportTime The new value of lastReportTime
   */
  public void setLastReportTime(long lastReportTime) {
    this.lastReportTime = lastReportTime;
  }

  /**
   * Convert a number of miliseconds since 1.1.1970 (epoch) to a current string date
   *
   * @param actualTimeInMseconds a time in miliseconds for the UTC time zone
   * @return String A time string of type HH:mm:ss such as 23:12:54.
   */
  public static String mSecsToString(long actualTimeInMseconds) {
    Date date = new Date(actualTimeInMseconds);
    DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    return (formatter.format(date));
  }
}
