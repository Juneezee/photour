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
 * A class for monitoring the ambientSensor temperature (not device temperature) of the environment
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class AmbientSensor {

  private static final String TAG = AmbientSensor.class.getSimpleName();
  private static final long AMBIENT_READING_FREQUENCY = 20000; // Reads every 20 seconds
  private static final long STOPPING_THRESHOLD = 20000; // Stop after inactive for 20 seconds

  private SensorEventListener temperatureListener = null;
  private SensorManager sensorManager;
  private Sensor ambientSensor;

  private Accelerometer accelerometer;

  private long samplingRateInMSecs;
  private long samplingRateNano;
  private long timePhoneWasLastRebooted;
  private long lastReportTime = 0;

  private MutableLiveData<Float> temperature = new MutableLiveData<>();
  private boolean started;

  /**
   * Constructor for the {@link AmbientSensor} class
   *
   * @param context The context of the current application
   */
  AmbientSensor(Context context) {
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    samplingRateNano = AMBIENT_READING_FREQUENCY * 1000000;
    samplingRateInMSecs = AMBIENT_READING_FREQUENCY;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    if (sensorManager != null) {
      ambientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    initThermometerListener();
  }

  /**
   * Initialise the listener and establishes the actions to take when a reading is available
   */
  private void initThermometerListener() {
    if (standardAmbientSensorAvailable()) {
      Log.d(TAG, "Using AmbientSensor Sensor");

      temperatureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          onAmbientSensorChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };

    } else {
      Log.d(TAG, "Standard AmbientSensor Sensor unavailable");
    }
  }

  /**
   * When the reading of ambient sensor has changed, store the value and update last report time
   *
   * @param event A {@link SensorEvent} instance
   */
  private void onAmbientSensorChanged(SensorEvent event) {
    long diff = event.timestamp - lastReportTime;
    // time is in nanoseconds it represents the set reference times the first time we come here
    // set event timestamp to current time in milliseconds
    // see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
    // the following operation avoids reporting too many events too quickly - the sensor may always
    // misbehave and start sending data very quickly
    if (diff >= samplingRateNano) {
      temperature.setValue(event.values[0]);
      int accuracy = event.accuracy;
      lastReportTime = event.timestamp;

      Log.d(TAG, "Temperature: " + temperature.getValue() + "\t\tAccuracy: " + accuracy);

      // if we have not see any movement on the side of the accelerometer, let's stop
      long actualTimeInMSecs =
          timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
      long timeLag = actualTimeInMSecs - accelerometer.getLastReportTime();

      if (timeLag > STOPPING_THRESHOLD) {
        stopAmbientSensor();
      }
    }
  }

  /**
   * Start monitoring the ambientSensor temperature
   *
   * @param accelerometer A {@link Accelerometer} instance
   */
  void startSensingTemperature(Accelerometer accelerometer) {
    this.accelerometer = accelerometer;

    // if the sensor is null, then sensorManager is null and we get a crash
    if (standardAmbientSensorAvailable()) {
      Log.d(TAG, "Starting AmbientSensor listener");

      // delay is in microseconds (1millisecond=1000 microseconds)
      // it does not seem to work though
      //stopBarometer();
      // otherwise we stop immediately because
      sensorManager.registerListener(temperatureListener, ambientSensor,
          (int) (samplingRateInMSecs * 1000));
      setStarted(true);
    } else {
      Log.d(TAG, "AmbientSensor unavailable or already active");
    }
  }

  /**
   * Get the value of ambient temperature
   *
   * @return LiveData<Float> The current ambient temperature
   */
  public LiveData<Float> getTemperature() {
    return temperature;
  }

  /**
   * Check if the device has a barometer
   *
   * @return boolean True if the device has barometer
   */
  private boolean standardAmbientSensorAvailable() {
    return ambientSensor != null;
  }

  /**
   * Stop the ambient sensor
   */
  void stopAmbientSensor() {
    if (standardAmbientSensorAvailable()) {
      Log.d(TAG, "Stopping AmbientSensor listener");

      try {
        sensorManager.unregisterListener(temperatureListener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }

    setStarted(false);
  }

  /**
   * Check if the ambientSensor temperature sensor is started on the current device
   *
   * @return boolean True if the ambientSensor temperature sensor is started
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
