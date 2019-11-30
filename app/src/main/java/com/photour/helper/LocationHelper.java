package com.photour.helper;

import static com.photour.ui.visitnew.NewVisitFragment.REQUEST_CHECK_SETTINGS;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.libraries.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Helper class for location related tasks
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class LocationHelper {

  /**
   * Check if the status of the device location (either ON of OFF)
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param fragment The fragment that calls this method
   * @param listener A {@link LocationServicesListener} instance for callback
   */
  public static void checkDeviceLocation(
      Activity activity,
      Fragment fragment,
      LocationServicesListener listener
  ) {
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
    builder.addLocationRequest(
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
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
   * Check if the new LatLng should be added into the latLngList
   *
   * @param latLngList The ArrayList of LatLng
   * @param newLatLng The new LatLng
   * @return boolean {@code true} if the new LatLng is more than 5 metres in distance than the last
   * LatLng
   */
  public static boolean shouldAddToLatLntList(ArrayList<LatLng> latLngList, LatLng newLatLng) {
    // First location updates, must add
    if (latLngList.isEmpty()) {
      return true;
    }

    // Check if distance with last location updates is >= 5 metres
    LatLng lastLatLng = latLngList.get(latLngList.size() - 1);
    float[] results = new float[3];
    Location.distanceBetween(
        lastLatLng.latitude,
        lastLatLng.longitude,
        newLatLng.latitude,
        newLatLng.longitude,
        results
    );

    return results[0] >= 5;
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
