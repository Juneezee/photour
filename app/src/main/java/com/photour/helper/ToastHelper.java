package com.photour.helper;

import android.app.Activity;
import android.widget.Toast;

/**
 * Helper class to show toast
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ToastHelper {

  /**
   * Show a short duration toast
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param text The text to show
   */
  public static void tShort(Activity activity, String text) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
  }
}
