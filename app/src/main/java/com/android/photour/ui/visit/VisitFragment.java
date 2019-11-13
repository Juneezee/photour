package com.android.photour.ui.visit;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.android.photour.R;
import com.android.photour.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.snackbar.Snackbar;
import java.util.Objects;

/**
 * Fragment for New Visit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitFragment extends Fragment {

  private VisitViewModel visitViewModel;
  private Activity activity;

  private final int LOCATION_PERMISSION_CODE = 9001;
  private static final int PLAY_SERVICES_ERROR_CODE = 9002;
  private final int REQUEST_CHECK_SETTINGS = 214;
  private static final int LOCATION_INTERVAL = 20000;
  private static final int FAST_LOCATION_INTERVAL = 10000;

  /**
   * Called to have the fragment instantiate its user interface view.
   *
   * @param inflater The LayoutInflater object that can be used to inflate any views in the
   * fragment,
   * @param container If non-null, this is the parent view that the fragment's UI should be attached
   * to.  The fragment should not add the view itself, but this can be used to generate the
   * LayoutParams of the view.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   * @return View Return the View for the fragment's UI, or null.
   */
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
    this.activity = getActivity();

    return inflater.inflate(R.layout.fragment_visit, container, false);
  }

  /**
   * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
   * but before any saved state has been restored in to the view.
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Set the date / time of TextClock
    final TextClock textClock = view.findViewById(R.id.textclock);
    textClock.setFormat24Hour("EEEE, dd MMMM yyyy \n\n HH:mm:ss");
    textClock.setFormat12Hour("EEEE, dd MMMM yyyy \n\n h:mm:ss a");

    startNewVisitListener(view);
  }

  /**
   * Add a click listener to the Start button, to replace current fragment with {@link
   * StartVisitFragment}
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void startNewVisitListener(View view) {
    view.findViewById(R.id.button_start_visit).setOnClickListener(v -> {

      if (checkPlayServices()) {
        // The device has the Google Play Services installed and compatible

        if (checkLocationPermission()) {
          // The device has granted location permission
          // Check if device location is turned on
          checkDeviceLocation();
        } else {
          requestLocationPermission();
        }
      } else {
        Snackbar.make(view, "Play services not available", Snackbar.LENGTH_LONG);
      }
    });
  }

  /**
   * Checks if the device has Google Play Services installed and compatible
   *
   * @return True if the device has Google Play Services installed and compatible
   */
  private boolean checkPlayServices() {
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

  /**
   * Checks if the user has granted location permission to the application
   *
   * @return True if the location permission is already granted
   */
  private boolean checkLocationPermission() {
    return ContextCompat.checkSelfPermission(activity, permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Requests the user to grant (or deny) location permission of the application
   */
  private void requestLocationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        ContextCompat.checkSelfPermission(activity, permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
    ) {
      requestPermissions(new String[]{permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);

//      if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)
//          || shouldShowRequestPermissionRationale(permission.CAMERA)
//          || shouldShowRequestPermissionRationale(permission.WRITE_EXTERNAL_STORAGE)) {
//        // Permission is not granted, show explanation
//
//      } else {
//        // No explanation needed
//
//      }

    }
  }

  /**
   * Callback for the result from requesting permissions. This method is invoked for every call on
   * {@link #requestPermissions(String[], int)}.
   *
   * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
   * @param permissions The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either {@link
   * android.content.pm.PackageManager#PERMISSION_GRANTED} or {@link android.content.pm.PackageManager#PERMISSION_DENIED}.
   * Never null.
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == LOCATION_PERMISSION_CODE
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      ToastHelper.tShort(activity, "Location permission granted");
    } else {
      ToastHelper.tShort(activity, "Location permission denied");
    }
  }

  /**
   * Check if the status of the device location (either ON of OFF)
   */
  private void checkDeviceLocation() {
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
    builder.addLocationRequest(LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(LOCATION_INTERVAL)
        .setFastestInterval(FAST_LOCATION_INTERVAL)
    );
    builder.setAlwaysShow(true);

    LocationSettingsRequest mLocationSettingsRequest = builder.build();

    LocationServices
        .getSettingsClient(activity)
        .checkLocationSettings(mLocationSettingsRequest)

        .addOnSuccessListener(
            // Device location is turned on, and location permission granted
            locationSettingsResponse -> navigateToStartVisit())

        .addOnFailureListener(e -> {
          // Device location is off, location permission can either be granted or denied
          if (((ApiException) e).getStatusCode()
              == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
            try {
              /*
               * Create a dialog (like Google Maps does) to turn ON device location after the user has
               * pressed OK
               */
              ResolvableApiException rae = (ResolvableApiException) e;
              startIntentSenderForResult(rae.getResolution().getIntentSender(),
                  REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
            } catch (IntentSender.SendIntentException sie) {
              Log.e("GPS", "Unable to execute request.");
            }
          }
        });
  }

  /**
   * Receive the result from a previous call to {@link #startActivityForResult(Intent, int)}.
   *
   * @param requestCode The integer request code originally supplied to startActivityForResult(),
   * allowing you to identify who this result came from.
   * @param resultCode The integer result code returned by the child activity through its
   * setResult().
   * @param data An Intent, which can return result data to the caller (various data can be attached
   * to Intent "extras").
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
      // The user has pressed OK on the dialog
      navigateToStartVisit();
    } else {
      ToastHelper.tShort(activity, "Device location is off");
    }
  }

  /**
   * Navigate to the start visit fragment. Condition: Device location turned on, location permission
   * granted
   */
  private void navigateToStartVisit() {
    Navigation.findNavController(Objects.requireNonNull(getView()))
        .navigate(R.id.action_start_visit);
  }
}
