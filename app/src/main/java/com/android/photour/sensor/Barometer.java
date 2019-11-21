package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * A class for monitoring the barometric pressure of the environment
 *
 * @author Professor Fabio Ciravegna
 */
public class Barometer {

  private static final String TAG = Barometer.class.getSimpleName();
  private static final long BAROMETER_READING_FREQUENCY = 20000; // Reads every 20 seconds
  private static final long STOPPING_THRESHOLD = 20000; // Stop after inactive for 20 seconds

  private SensorEventListener pressureListener = null;
  private SensorManager sensorManager;
  private Sensor barometer;

  private Accelerometer accelerometer;

  private long samplingRateInMSecs;
  private long samplingRateNano;
  private long timePhoneWasLastRebooted;
  private long lastReportTime = 0;

  private MutableLiveData<Float> pressure = new MutableLiveData<>();
  private boolean started;

  /**
   * Constructor for the {@link Barometer} class
   *
   * @param context The context of the current application
   */
  Barometer(Context context) {
    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    samplingRateNano = BAROMETER_READING_FREQUENCY * 1000000;
    samplingRateInMSecs = BAROMETER_READING_FREQUENCY;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    if (sensorManager != null) {
      barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

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
          onBarometerChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };
    } else {
      Log.d(TAG, "Standard Barometer unavailable");
    }
  }

  /**
   * When the reading of barometer has changed, store the value and update the last report time
   *
   * @param event A {@link SensorEvent} instance
   */
  private void onBarometerChanged(SensorEvent event) {
    long diff = event.timestamp - lastReportTime;
    // time is in nanoseconds it represents the set reference times the first time we come here
    // set event timestamp to current time in milliseconds
    // see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
    // the following operation avoids reporting too many events too quickly - the sensor may always
    // misbehave and start sending data very quickly
    if (diff >= samplingRateNano) {
      pressure.setValue(event.values[0]);
      int accuracy = event.accuracy;
      lastReportTime = event.timestamp;

      Log.d(TAG, "Barometric pressure: " + pressure.getValue() + "\t\tAccuracy: " + accuracy);

      // if we have not see any movement on the side of the accelerometer, let's stop
      long actualTimeInMSecs =
          timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
      long timeLag = actualTimeInMSecs - accelerometer.getLastReportTime();

      if (timeLag > STOPPING_THRESHOLD) {
        stopBarometer();
      }
    }
  }

  /**
   * Start monitoring the barometric pressure
   *
   * @param accelerometer A {@link Accelerometer} instance
   */
  void startSensingPressure(Accelerometer accelerometer) {
    this.accelerometer = accelerometer;
    // if the sensor is null, then sensorManager is null and we get a crash
    if (standardPressureSensorAvailable()) {
      Log.d(TAG, "Starting Barometer listener");
      // delay is in microseconds (1millisecond=1000 microseconds)
      // it does not seem to work though
      //stopBarometer();
      // otherwise we stop immediately because
      sensorManager.registerListener(pressureListener, barometer,
          (int) (samplingRateInMSecs * 1000));
      setStarted(true);
    } else {
      Log.d(TAG, "Barometer unavailable or already active");
    }
  }

  /**
   * Get the value of barometric pressure
   *
   * @return LiveData<Float> The current barometric pressure in hPa
   */
  public LiveData<Float> getPressure() {
    return pressure;
  }

  /**
   * Check if the device has a barometer
   *
   * @return boolean True if the device has barometer
   */
  private boolean standardPressureSensorAvailable() {
    return barometer != null;
  }

  /**
   * Stop the barometer
   */
  void stopBarometer() {
    if (standardPressureSensorAvailable()) {
      Log.d(TAG, "Stopping Barometer listener");
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
  boolean isStarted() {
    return started;
  }

  /**
   * Set the value of the started field
   *
   * @param started The new value of the started field
   */
  private void setStarted(boolean started) {
    this.started = started;
  }
}
