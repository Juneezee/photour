package com.photour.helper;

import java.io.File;

/**
 * Helper class to check if file exists
 *
 * @author Jia Hua Ng, Eng Zer Jun
 */
public class FileHelper {

  /**
   * Checks if filepath is valid
   *
   * @param filePath String of filepath
   * @return true if the filepath is valid, else return false
   */
  public static Boolean fileExist(String filePath) {
    return filePath != null && new File(filePath).exists();
  }
}
