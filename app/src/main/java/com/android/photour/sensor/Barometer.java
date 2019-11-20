package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * A class for monitoring the barometric pressure of the environment
 *
 * @author Professor Fabio Ciravegna
 */
public class Barometer {

  private static final String TAG = Barometer.class.getSimpleName();

  private SensorEventListener pressureListener = null;
  private SensorManager sensorManager;
  private Sensor barometer;

  private Accelerometer accelerometer;

  private long samplingRateInMSecs;
  private long samplingRateNano;
  private long timePhoneWasLastRebooted;
  private long BAROMETER_READING_FREQUENCY = 20000;
  private long lastReportTime = 0;

  private boolean started;

  /**
   * this is used to stop the barometer if we have not seen any movement in the last 20 seconds
   */
  private static final long STOPPING_THRESHOLD = 20000;

  /**
   * Constructor for the {@link Barometer} class
   *
   * @param context The context of the current application
   */
  public Barometer(Context context) {
    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    samplingRateNano = BAROMETER_READING_FREQUENCY * 1000000;
    samplingRateInMSecs = BAROMETER_READING_FREQUENCY;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    initBarometerListener();
  }

  /**
   * Initialise the listener and establishes the actions to take when a reading is available
   */
  private void initBarometerListener() {
    if (standardPressureSensorAvailable()) {
      Log.d(TAG, "Using Barometer");
      pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          long diff = event.timestamp - lastReportTime;
          // time is in nanoseconds it represents the set reference times the first time we come here
          // set event timestamp to current time in milliseconds
          // see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
          // the following operation avoids reporting too many events too quickly - the sensor may always
          // misbehave and start sending data very quickly
          if (diff >= samplingRateNano) {
            long actualTimeInMseconds =
                timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
            float pressureValue = event.values[0];
            int accuracy = event.accuracy;

            lastReportTime = event.timestamp;
            // if we have not see any movement on the side of the accelerometer, let's stop
            long timeLag = actualTimeInMseconds - accelerometer.getLastReportTime();
            if (timeLag > STOPPING_THRESHOLD) {
              stopBarometer();
            }
          }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
      };
    } else {
      Log.d(TAG, "Standard Barometer unavailable");
    }

  }

  /**
   * Check if the device has a barometer
   *
   * @return boolean True if the device has barometer
   */
  public boolean standardPressureSensorAvailable() {
    return barometer != null;
  }

  /**
   * Start monitoring the barometric pressure
   *
   * @param accelerometer A {@link Accelerometer} instance
   */
  public void startSensingPressure(Accelerometer accelerometer) {
    this.accelerometer = accelerometer;
    // if the sensor is null,then sensorManager is null and we get a crash
    if (standardPressureSensorAvailable()) {
      Log.d("Standard Barometer", "starting listener");
      // delay is in microseconds (1millisecond=1000 microseconds)
      // it does not seem to work though
      //stopBarometer();
      // otherwise we stop immediately because
      sensorManager.registerListener(pressureListener, barometer,
          (int) (samplingRateInMSecs * 1000));
      setStarted(true);
    } else {
      Log.i(TAG, "barometer unavailable or already active");
    }
  }

  /**
   * Stop the barometer
   */
  public void stopBarometer() {
    if (standardPressureSensorAvailable()) {
      Log.d("Standard Barometer", "Stopping listener");
      try {
        sensorManager.unregisterListener(pressureListener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }
    setStarted(false);
  }

  /**
   * Check if the barometer is started on the current device
   *
   * @return boolean True if the barometer is started
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * Set the value of the started field
   *
   * @param started The new value of the started field
   */
  public void setStarted(boolean started) {
    this.started = started;
  }
}
