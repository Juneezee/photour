package com.photour.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A helper class for formatting date
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class DateHelper {

  /**
   * Format the date into dd MMMM yyyy (e.g. 01 December 2019)
   *
   * @param date The {@link Date} object
   * @return String The formatted date in dd MMMM yyyy (e.g. 01 December 2019)
   */
  public static String regularFormat(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    return sdf.format(date);
  }

  /**
   * Format the date into E, dd MMM yyyy • HH:mm (e.g. Sun, 01 Dec 2019 • 23:13)
   *
   * @param date The {@link Date} object
   * @return String The formatted date in E, dd MMM yyyy • HH:mm (e.g. Sun, 01 Dec 2019 • 23:13)
   */
  public static String regularFormatWithNameTime(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy • HH:mm", Locale.getDefault());
    return sdf.format(date);
  }

  /**
   * Convert miliseconds into X days X hours X minutes X seconds format
   *
   * @param miliseconds The number of miliseconds
   * @return String The formatted string
   */
  public static String elapsedTimeFormat(long miliseconds) {
    long days = TimeUnit.MILLISECONDS.toDays(miliseconds);
    miliseconds -= TimeUnit.DAYS.toMillis(days);
    long hours = TimeUnit.MILLISECONDS.toHours(miliseconds);
    miliseconds -= TimeUnit.HOURS.toMillis(hours);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(miliseconds);
    miliseconds -= TimeUnit.MINUTES.toMillis(minutes);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(miliseconds);

    StringBuilder sb = new StringBuilder(64);

    if (days > 0) {
      sb.append(days);
      sb.append(" day").append(days == 1 ? " " : "s ");
    }

    if (hours > 0) {
      sb.append(hours);
      sb.append(" hour").append(hours == 1 ? " " : "s ");
    }

    if (minutes > 0) {
      sb.append(minutes);
      sb.append(" minute").append(minutes == 1 ? " " : "s ");
    }

    if (seconds > 0) {
      sb.append(seconds);
      sb.append(" second").append(seconds == 1 ? " " : "s");
    }

    return sb.toString();
  }
}
