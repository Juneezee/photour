package com.android.photour.helper;

import static com.android.photour.ui.visit.VisitFragment.REQUEST_CHECK_SETTINGS;

import android.app.Activity;
import android.content.IntentSender;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Helper class for checking the status of location services (device location)
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class LocationServicesHelper {

  private static final int LOCATION_INTERVAL = 20000;
  private static final int FAST_LOCATION_INTERVAL = 5000;

  /**
   * Check if the status of the device location (either ON of OFF)
   *
   * @param activity The current activity
   * @param fragment The fragment that calls this method
   * @param listener A {@link LocationServicesListener} instance for callback
   */
  public static void checkDeviceLocation(Activity activity, Fragment fragment,
      LocationServicesListener listener) {
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
    builder.addLocationRequest(LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(LOCATION_INTERVAL)
        .setFastestInterval(FAST_LOCATION_INTERVAL)
    );
    builder.setAlwaysShow(true);

    LocationSettingsRequest locationSettingsRequest = builder.build();

    LocationServices
        .getSettingsClient(activity)
        .checkLocationSettings(locationSettingsRequest)

        .addOnSuccessListener(
            // Device location is turned on, and location permission granted
            locationSettingsResponse -> listener.onSuccess())

        .addOnFailureListener(e -> locationServicesFail(e, activity, fragment));
  }

  /**
   * Device location if OFF . Prompt a dialog that allows the user to click OK and automatically
   * turn ON device location
   *
   * @param e Exception class instance
   */
  private static void locationServicesFail(Exception e, Activity activity, Fragment fragment) {
    final int statusCode = ((ApiException) e).getStatusCode();

    // Device location is off, location permission can either be granted or denied
    if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
      try {
        /*
         * Create a dialog (like Google Maps does) to turn ON device location after the user has
         * pressed OK
         */
        ResolvableApiException rae = (ResolvableApiException) e;
        fragment.startIntentSenderForResult(rae.getResolution().getIntentSender(),
            REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
      } catch (IntentSender.SendIntentException sie) {
        ToastHelper.tShort(activity, "Unable to execute request");
      }

    } else if (statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
      // When WiFi off and Cellular Off, this will happen
      ToastHelper.tShort(activity,
          "Unable to find GPS location (try turning on WiFi or mobile signal)");
    }
  }

  /**
   * Callback interface for handling success case when requesting device location
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public interface LocationServicesListener {

    /**
     * Callback on device location turned ON
     */
    void onSuccess();
  }
}
