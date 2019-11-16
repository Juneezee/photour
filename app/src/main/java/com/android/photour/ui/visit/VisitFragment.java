package com.android.photour.ui.visit;

import static com.android.photour.helper.PermissionHelper.ALL_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.CAMERA_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.CS_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LC_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LOCATION_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LS_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.NO_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.android.photour.R;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.PermissionHelper.PermissionCodeResponse;
import com.android.photour.helper.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import java.util.Objects;

/**
 * Fragment for New Visit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitFragment extends Fragment {

  private VisitViewModel visitViewModel;
  private Activity activity;

  private static final String[] ALL_PERMISSIONS_REQUIRED = {
      permission.ACCESS_FINE_LOCATION,
      permission.CAMERA,
      permission.WRITE_EXTERNAL_STORAGE
  };
  private static final int REQUEST_CHECK_SETTINGS = 214;
  private static final int PLAY_SERVICES_ERROR_CODE = 9002;
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
    textClock.setFormat24Hour("EEEE, dd MMMM yyyy\n\nHH:mm:ss");
    textClock.setFormat12Hour("EEEE, dd MMMM yyyy\n\nh:mm:ss a");

    startNewVisitListener(view);
  }


  /**
   * Navigate to the start visit fragment. Condition: Device location turned on, location permission
   * granted
   */
  private void navigateToStartVisit() {
    Navigation.findNavController(Objects.requireNonNull(getView()))
        .navigate(R.id.action_start_visit);
  }

  /**
   * Add a click listener to the Start button, to replace current fragment with {@link
   * StartVisitFragment} after explicit permissions check has passed
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void startNewVisitListener(View view) {
    view.findViewById(R.id.button_start_visit).setOnClickListener(v -> {

      if (checkPlayServices()) {
        boolean isFirstTime = PermissionHelper
            .isFirstTimeAskingPermissions(activity, ALL_PERMISSIONS_REQUIRED);

        int permissionGranted = ALL_PERMISSIONS_CODE - permissionsNotGranted(false);
        int permissionsNeverAsked = permissionsNotGranted(true) - permissionGranted;

        Log.d("Perm", "Permission granted " + permissionGranted);
        Log.d("Perm", "Permission not granted " + (ALL_PERMISSIONS_CODE - permissionGranted));
        Log.d("Perm", "Permission never asked " + permissionsNeverAsked);

        // Display a dialog for permissions explanation
        showPermissionRationale(isFirstTime, permissionGranted, permissionsNeverAsked);

      } else {
        ToastHelper.tShort(activity, "Play services not available");
      }
    });
  }

  /**
   * Checks if the device has Google Play Services installed and compatible
   *
   * @return boolean True if the device has Google Play Services installed and compatible
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
   * Get the permissions not granted by user due to denying, or get the permissions not granted by
   * user due to "Never ask again"
   *
   * shouldShowRequestPermissionRationale() returns true if the user has previously denied the
   * request, and returns false if first time, or a permission is allowed, or a user has denied a
   * permission and selected the Don't ask again option
   *
   * @param checkNeverAsk True if to get the permissions that are set as "Never ask again", False to
   * get the permissions not granted by deny only
   * @return int The permission request code
   */
  private int permissionsNotGranted(boolean checkNeverAsk) {
    boolean locationPermission = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)
        : PermissionHelper.hasLocationPermission(activity);
    boolean cameraPermission = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(permission.CAMERA)
        : PermissionHelper.hasCameraPermission(activity);
    boolean storagePermission = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(permission.WRITE_EXTERNAL_STORAGE)
        : PermissionHelper.hasStoragePermission(activity);

    int permissionCode = ALL_PERMISSIONS_CODE;

    if (locationPermission && cameraPermission && storagePermission) {
      // All permissions granted, check if device location is turned on
      permissionCode = NO_PERMISSIONS_CODE;

    } else if (!locationPermission && cameraPermission && storagePermission) {
      // No permission for location; camera and storage granted
      permissionCode = LOCATION_PERMISSION_CODE;

    } else if (locationPermission && !cameraPermission && storagePermission) {
      // Permission granted for location; no permission for camera and storage
      permissionCode = CAMERA_PERMISSION_CODE;

    } else if (locationPermission && cameraPermission) {
      // Permission granted for location and camera; no permission for storage
      permissionCode = STORAGE_PERMISSION_CODE;

    } else if (!locationPermission && !cameraPermission && storagePermission) {
      // Permission granted for storage; no permission for location and camera
      permissionCode = LC_PERMISSION_CODE;

    } else if (!locationPermission && cameraPermission) {
      // Permission granted for camera; no permission for location and storage
      permissionCode = LS_PERMISSION_CODE;

    } else if (locationPermission) {
      // Permission granted for location; no permission for camera and storage
      permissionCode = CS_PERMISSION_CODE;
    }

    return checkNeverAsk ? ALL_PERMISSIONS_CODE - permissionCode : permissionCode;
  }

  private void showPermissionRationale(
      boolean isFirstTime,
      int permissionGranted,
      int permissionsNeverAsked
  ) {
    int permissionNotGranted = ALL_PERMISSIONS_CODE - permissionGranted;
    boolean isAllPermissionsAllowed = permissionGranted == ALL_PERMISSIONS_CODE;
    boolean anyNeverAskChecked = !isFirstTime && permissionsNeverAsked != NO_PERMISSIONS_CODE
        && permissionsNeverAsked != permissionGranted;

    int permissionToRequest
        = isFirstTime ? ALL_PERMISSIONS_CODE
        : isAllPermissionsAllowed ? NO_PERMISSIONS_CODE
            : anyNeverAskChecked ? permissionsNeverAsked | permissionNotGranted : permissionNotGranted;

    PermissionCodeResponse codeResponse = PermissionHelper.PERMISSIONS_MAP.get(permissionToRequest);

    String message = "To capture and upload photos with location tag, allow Photour access to your "
        + "device's %s. "
        + (anyNeverAskChecked ? "Tap Settings > Permissions, and turn %s." : "");

    message = String
        .format(message, codeResponse.getRationaleName(), codeResponse.getRationaleNameOn());

    if (isAllPermissionsAllowed) {
      checkRequiredPermissions(permissionToRequest);
    } else {
      int titleLayout = codeResponse.getLayout();

      buildDialog(titleLayout, message, permissionToRequest, anyNeverAskChecked);
    }
  }

  /**
   * 1. If shouldShowSettingsDialog is true, then the user has checked "Never ask again" for any
   * permissions. The dialog should show Settings as positive button that brings the user to
   * application settings page to enable permissions
   *
   * 2. If shouldShowSettingsDialog is false, then the user has not checked "Never ask again" for
   * any permissions. The dialog should show Continue as positive button that keeps asking the user
   * to grant permissions
   *
   * @param titleLayout The layout ID for the ImageView
   * @param message The message of the dialog
   * @param permissionToRequest The permission request code
   * @param shouldShowSettingsDialog True if the user has checked "Never ask again" for any
   * permissions
   */
  private void buildDialog(
      int titleLayout,
      String message,
      int permissionToRequest,
      boolean shouldShowSettingsDialog
  ) {
    AlertDialog.Builder builder = new Builder(activity);
    builder.setMessage(message);
    builder.setCustomTitle(activity.getLayoutInflater().inflate(titleLayout, null));

    builder
        .setPositiveButton(shouldShowSettingsDialog ? "SETTINGS" : "CONTINUE", (dialog, which) -> {
          if (shouldShowSettingsDialog) {
            Uri uri = new Uri.Builder()
                .scheme("package")
                .opaquePart(activity.getPackageName())
                .build();
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
          } else {
            PermissionHelper.setFirstTimeAskingPermissions(activity, ALL_PERMISSIONS_REQUIRED);
            checkRequiredPermissions(permissionToRequest);
          }
        }).setNegativeButton("NOT NOW", (dialog, which) -> dialog.dismiss());

    builder.create().show();
  }

  /**
   * Check if the applications need to ask for permissions or not
   *
   * @param permissionToRequest The permission request code
   */
  private void checkRequiredPermissions(int permissionToRequest) {
    if (permissionToRequest == NO_PERMISSIONS_CODE) {
      // All permissions required are granted, check if device location is ON
      checkDeviceLocation();

    } else {
      // No permissions for location, camera, and storage
      requestPermissions(ALL_PERMISSIONS_REQUIRED, permissionToRequest);
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

    boolean allPermissionsGranted = true;

    for (int grantResult : grantResults) {
      allPermissionsGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
    }

    PermissionCodeResponse codeResponse = PermissionHelper.PERMISSIONS_MAP.get(requestCode);

    String result = allPermissionsGranted ? "granted" : "denied";

    String message
        = requestCode == ALL_PERMISSIONS_CODE
        ? "Required permissions "
        : (codeResponse.getResponseResult());
    message += result;

    if (allPermissionsGranted) {
      checkDeviceLocation();
    }

    ToastHelper.tShort(activity, message);
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

        .addOnFailureListener(this::locationServicesFail);
  }

  /**
   * Device location if OFF . Prompt a dialog that allows the user to click OK and automatically
   * turn ON device location
   *
   * @param e Exception class instance
   */
  private void locationServicesFail(Exception e) {
    final int statusCode = ((ApiException) e).getStatusCode();

    // Device location is off, location permission can either be granted or denied
    if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
      try {
        /*
         * Create a dialog (like Google Maps does) to turn ON device location after the user has
         * pressed OK
         */
        ResolvableApiException rae = (ResolvableApiException) e;
        startIntentSenderForResult(rae.getResolution().getIntentSender(),
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
}
