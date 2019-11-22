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
 * A parent class for {@link Barometer} and {@link AmbientSensor} for monitoring the sensor values
 *
 * @author Professor Fabio Ciravegna, Zer Jun Eng, Jia Hua Ng
 */
public class EnvironmentSensor {

  private String tag;
  private static final long READING_FREQUENCY = 20000; // Reads every 20 seconds
  private static final long STOPPING_THRESHOLD = 30000; // Stop after inactive for 30 seconds

  private SensorEventListener listener = null;
  private SensorManager sensorManager;
  private Sensor sensor;

  private Accelerometer accelerometer;

  private long samplingRateInMSecs;
  private long samplingRateNano;
  private long timePhoneWasLastRebooted;
  private long lastReportTime = 0;

  private MutableLiveData<Float> sensorValue = new MutableLiveData<>();
  private boolean started;

  /**
   * Constructor for {@link EnvironmentSensor} class
   *
   * @param context The context of current application
   * @param sensorType The sensor type to get
   */
  EnvironmentSensor(Context context, int sensorType) {
    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    samplingRateNano = READING_FREQUENCY * 1000000;
    samplingRateInMSecs = STOPPING_THRESHOLD;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    if (sensorManager != null) {
      sensor = sensorManager.getDefaultSensor(sensorType);
    }

    initSensorListener();
  }

  /**
   * Set the tag to use for debugging purposes
   *
   * @param tag The tag used for debugging purposes
   */
  void setTag(String tag) {
    this.tag = tag;
  }

  /**
   * Initialise the listener and establishes the actions to take when a reading is available
   */
  private void initSensorListener() {
    if (standardSensorAvailable()) {
      Log.d(tag, "Using " + tag + " sensor");

      listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          EnvironmentSensor.this.onSensorChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };

    } else {
      Log.d(tag, "Standard " + tag + " sensor unavailable");
    }
  }

  /**
   * When the reading of the sensor has changed, store the value and update last report time
   *
   * @param event A {@link SensorEvent} instance
   */
  private void onSensorChanged(SensorEvent event) {
    long diff = event.timestamp - lastReportTime;
    // time is in nanoseconds it represents the set reference times the first time we come here
    // set event timestamp to current time in milliseconds
    // see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
    // the following operation avoids reporting too many events too quickly - the sensor may always
    // misbehave and start sending data very quickly
    if (diff >= samplingRateNano) {
      sensorValue.setValue(event.values[0]);
      int accuracy = event.accuracy;
      lastReportTime = event.timestamp;

      Log.d(tag, tag + ": " + sensorValue.getValue() + "\t\tAccuracy: " + accuracy);

      // if we have not see any movement on the side of the accelerometer, let's stop
      long actualTimeInMSecs =
          timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
      long timeLag = actualTimeInMSecs - accelerometer.getLastReportTime();

      if (timeLag > STOPPING_THRESHOLD) {
        stopSensor();
      }
    }
  }

  /**
   * Start monitoring the sensor value
   *
   * @param accelerometer A {@link Accelerometer} instance
   */
  void startSensingValue(Accelerometer accelerometer) {
    this.accelerometer = accelerometer;

    // if the sensor is null, then sensorManager is null and we get a crash
    if (standardSensorAvailable()) {
      Log.d(tag, "Starting " + tag + " listener");

      // delay is in microseconds (1millisecond=1000 microseconds)
      // it does not seem to work though
      // stopBarometer();
      // otherwise we stop immediately because
      sensorManager.registerListener(listener, sensor, (int) (samplingRateInMSecs * 1000));
      setStarted(true);
    } else {
      Log.d(tag, tag + " unavailable or already active");
    }
  }

  /**
   * Get the reading value of the sensor
   *
   * @return LiveData<Float> The current ambient temperature
   */
  public LiveData<Float> getSensorValue() {
    return sensorValue;
  }

  /**
   * Check if the device has the sensor type
   *
   * @return boolean True if the device has barometer
   */
  private boolean standardSensorAvailable() {
    return sensor != null;
  }

  /**
   * Stop the sensor by unregistering it
   */
  void stopSensor() {
    if (standardSensorAvailable()) {
      Log.d(tag, "Stopping " + tag + " listener");

      try {
        sensorManager.unregisterListener(listener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }

    setStarted(false);
  }

  /**
   * Check if the sensor has not been started yet
   *
   * @return boolean True if the sensor has not been started yet
   */
  boolean isNotStarted() {
    return !started;
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
