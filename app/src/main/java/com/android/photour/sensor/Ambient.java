package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

/**
 * A class for monitoring the ambient temperature (not device temperature) of the environment
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class Ambient {

  private static final String TAG = Ambient.class.getSimpleName();

  private SensorEventListener temperatureListener = null;
  private SensorManager sensorManager;
  private Sensor ambient;

  private Accelerometer accelerometer;

  private long samplingRateInMSecs;
  private long samplingRateNano;
  private long timePhoneWasLastRebooted;
  private long AMBIENT_READING_FREQUENCY = 20000; // 20 seconds
  private long lastReportTime = 0;

  private boolean started;

  /**
   * Constructor for the {@link Ambient} class
   *
   * @param context The context of the current application
   */
  public Ambient(Context context) {
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();
    samplingRateNano = AMBIENT_READING_FREQUENCY * 1000000;
    samplingRateInMSecs = AMBIENT_READING_FREQUENCY;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    ambient = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
  }

  /**
   * Initialise the listener and establishes the actions to take when a reading is available
   */
  private void initThermometerListener() {

  }

  /**
   * Check if the device has a barometer
   *
   * @return boolean True if the device has barometer
   */
  public boolean standardPressureSensorAvailable() {
    return ambient != null;
  }

  /**
   * Check if the ambient temperature sensor is started on the current device
   *
   * @return boolean True if the ambient temperature sensor is started
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
