package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

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
  private AmbientSensor ambientSensor;

  private long timePhoneWasLastRebooted;
  private long lastReportTime = 0;
  private float lastX = 0;
  private float lastY = 0;
  private float lastZ = 0;

  /**
   * Constructor of the {@link Accelerometer} class
   *
   * @param context The context of the current application
   */
  public Accelerometer(Context context) {
    this.barometer = new Barometer(context);
    this.ambientSensor = new AmbientSensor(context);

    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    if (sensorManager != null) {
      accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

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
          onAcceleratorChanged(event);
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
   * When the reading of accelerometer has changed, start barometer and ambient sensor if necessary
   *
   * @param event A {@link SensorEvent} instance
   */
  private void onAcceleratorChanged(SensorEvent event) {
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
      Log.d(TAG, "Motion detected | x: " + deltaX + ", y: " + deltaY + ", z: " + deltaZ);

      // Start barometer
      if (barometer.isNotStarted()) {
        barometer.startSensingValue(Accelerometer.this);
      }

      // Start ambient sensor
      if (ambientSensor.isNotStarted()) {
        ambientSensor.startSensingValue(Accelerometer.this);
      }

      long actualTimeInMSecs = timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
      setLastReportTime(actualTimeInMSecs);
    }

    lastX = x;
    lastY = y;
    lastZ = z;
  }

  /**
   * Start monitoring the accelerator movement
   */
  public void startAccelerometerRecording() {
    // if the sensor is null,then sensorManager is null and we get a crash
    if (standardAccelerometerAvailable()) {
      Log.d(TAG, "Starting Accelerometer listener");
      // THE ACCELEROMETER receives as frequency a predefined subset of timing
      // https://developer.android.com/reference/android/hardware/SensorManager
      sensorManager.registerListener(accelerationListener, accelerometer,
          SensorManager.SENSOR_DELAY_UI);
    } else {
      Log.d(TAG, "Accelerometer unavailable or already active");
    }
  }

  /**
   * Get the {@link Barometer} instance
   *
   * @return Barometer A {@link Barometer} instance
   */
  public Barometer getBarometer() {
    return barometer;
  }

  /**
   * Get the {@link AmbientSensor} instance
   *
   * @return AmbientSensor A {@link AmbientSensor} instance
   */
  public AmbientSensor getAmbientSensor() {
    return ambientSensor;
  }

  /**
   * Check if accelerometer sensor is available on current device
   *
   * @return boolean True if the accelerometer sensor is available on current device
   */
  private boolean standardAccelerometerAvailable() {
    return accelerometer != null;
  }

  /**
   * Stop the accelerometer, barometer, and thermometer
   */
  public void stopAccelerometer() {
    if (standardAccelerometerAvailable()) {
      Log.d(TAG, "Stopping Accelerometer listener");
      try {
        sensorManager.unregisterListener(accelerationListener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }
    // Stop the barometer and ambient sensor
    barometer.stopSensor();
    ambientSensor.stopSensor();
  }

  /**
   * Get the lastReportTime, the time where a sensor last reported its reading
   *
   * @return long The value of lastReportTime
   */
  long getLastReportTime() {
    return lastReportTime;
  }

  /**
   * Set the new value of lastReportTime
   *
   * @param lastReportTime The new value of lastReportTime
   */
  private void setLastReportTime(long lastReportTime) {
    this.lastReportTime = lastReportTime;
  }
}
