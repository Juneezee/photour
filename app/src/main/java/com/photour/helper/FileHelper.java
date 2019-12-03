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
    if (filePath != null) {
      File temp = new File(filePath);
      return temp.exists();
    } else {
      return false;
    }
  }
}
