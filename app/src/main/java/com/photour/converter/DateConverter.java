package com.photour.converter;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Type converter for {@link Date}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class DateConverter {

  /**
   * Convert a {@link Long} timestamp to {@link Date} object
   *
   * @param value The {@link Long} timestamp
   * @return Date A {@link Date} object
   */
  @TypeConverter
  public static Date fromTimestamp(Long value) {
    return value == null ? null : new Date(value);
  }

  /**
   * Convert a {@link Date} object to {@link Long} timestamp
   *
   * @param date The {@link Date} object
   * @return Long The {@link Long} timestamp
   */
  @TypeConverter
  public static Long dateToTimestamp(Date date) {
    return date == null ? null : date.getTime();
  }
}
