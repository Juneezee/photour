package com.photour.helper;

import android.text.Editable;

/**
 * A helper class for String validation
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StringHelper {


  /**
   * Check if the provided string is valid (not null and not empty)
   *
   * @param s The string to validate
   * @return boolean {@code true} if the string is valid (not null and not empty), {@code false}
   * otherwise
   */
  public static boolean isValidString(String s) {
    return s != null && !s.trim().isEmpty();
  }

  /**
   * Check if the provided string is not valid (null or empty)
   *
   * @param s The string to validate
   * @return boolean {@code true} if the string is invalid (null or empty), {@code false} otherwise
   */
  public static boolean isInvalidString(String s) {
    return !isValidString(s);
  }

  /**
   * Check if the provided {@link Editable} is valid
   *
   * @return boolean {@code true} if the {@link Editable} is not null and contains text, {@code
   * false} otherwise
   */
  public static boolean isValidEditable(Editable editable) {
    return editable != null && !editable.toString().trim().isEmpty();
  }
}
