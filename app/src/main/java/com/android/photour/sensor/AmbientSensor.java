package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;

/**
 * A child class of {@link EnvironmentSensor}, setting the sensor type to be
 * TYPE_AMBIENT_TEMPERATURE
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class AmbientSensor extends EnvironmentSensor {

  private static final String TAG = AmbientSensor.class.getSimpleName();

  /**
   * Constructor for the {@link AmbientSensor} class
   *
   * @param context The context of the current application
   */
  AmbientSensor(Context context) {
    super(context, Sensor.TYPE_AMBIENT_TEMPERATURE);
    super.setTag(TAG);
  }
}
