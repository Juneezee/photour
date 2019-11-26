package com.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;

/**
 * A child class of {@link EnvironmentSensor}, setting the sensor type to be TYPE_PRESSURE
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class Barometer extends EnvironmentSensor {

  private static final String TAG = Barometer.class.getSimpleName();

  /**
   * Constructor for the {@link Barometer} class
   *
   * @param context The context of the current application
   */
  Barometer(Context context) {
    super(context, Sensor.TYPE_PRESSURE, TAG);
  }
}
