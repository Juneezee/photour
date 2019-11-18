package com.android.photour.helper;

import android.app.Activity;
import android.app.Dialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Helper class to check the availability of play services in the device
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PlayServicesHelper {

  private static final int PLAY_SERVICES_ERROR_CODE = 9002;

  /**
   * Checks if the device has Google Play Services installed and compatible
   *
   * @return boolean True if the device has Google Play Services installed and compatible
   */
  public static boolean checkPlayServices(Activity activity) {
    GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

    int result = googleApi.isGooglePlayServicesAvailable(activity);

    if (result == ConnectionResult.SUCCESS) {
      return true;
    } else if (googleApi.isUserResolvableError(result)) {
      Dialog dialog = googleApi.getErrorDialog(activity, result, PLAY_SERVICES_ERROR_CODE,
          task -> ToastHelper.tShort(activity, "Dialog is cancelled"));
      dialog.show();
    } else {
      ToastHelper.tShort(activity, "Play services are required by this application");
    }

    return false;
  }
}
