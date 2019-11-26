package com.photour.converter;

import androidx.room.TypeConverter;

/**
 * Type converter for float array
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class FloatArrayConverter {

  /**
   * Convert float array to comma separated string
   *
   * @param values The array of float values
   * @return The comma separated string
   */
  @TypeConverter
  public static String fromArray(float[] values) {
    if (values == null || values.length == 0) {
      return "";
    }

    final StringBuilder string = new StringBuilder();

    for (float value : values) {
      string.append(value).append(",");
    }

    return string.toString();
  }

  /**
   * Convert comma separated string to float array
   *
   * @param string The comma separated string
   * @return The array of float values
   */
  @TypeConverter
  public static float[] toArray(String string) {
    if (string.isEmpty()) {
      return null;
    }

    final String[] arr = string.split(".");
    final float[] values = new float[arr.length];

    for (int i = 0; i < arr.length; i++) {
      values[i] = Float.parseFloat(arr[i]);
    }

    return values;
  }
}
