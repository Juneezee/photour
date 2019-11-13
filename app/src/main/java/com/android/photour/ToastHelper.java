package com.android.photour;

import android.app.Activity;
import android.widget.Toast;

public class ToastHelper {

  public static void tShort(Activity activity, String text) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
  }
}
