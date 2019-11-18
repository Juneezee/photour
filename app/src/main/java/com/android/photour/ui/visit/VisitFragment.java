package com.android.photour.ui.visit;

import static com.android.photour.helper.LocationServicesHelper.checkDeviceLocation;
import static com.android.photour.helper.PermissionHelper.ALL_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.CAMERA_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LOCATION_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.NO_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;
import static com.android.photour.helper.PlayServicesHelper.checkPlayServices;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.android.photour.R;
import com.android.photour.helper.AlertDialogHelper;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.PermissionHelper.PermissionCodeResponse;
import com.android.photour.helper.ToastHelper;
import com.android.photour.ui.visit.VisitFragmentDirections.ActionStartVisit;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

/**
 * Fragment for New Visit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitFragment extends Fragment {

  public static final int REQUEST_CHECK_SETTINGS = 214;
  private static final String[] PERMISSIONS_REQUIRED = {
      permission.ACCESS_FINE_LOCATION,
      permission.CAMERA,
      permission.WRITE_EXTERNAL_STORAGE
  };

  private Activity activity;
  private View view;

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

    activity = getActivity();
    view = inflater.inflate(R.layout.fragment_visit, container, false);

    return view;
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

    startNewVisitListener();
  }

  /**
   * Navigate to the start visit fragment. Condition: Device location turned on, location permission
   * granted
   */
  private void navigateToStartVisit() {
    TextInputEditText newVisitTitle = view.findViewById(R.id.new_visit_title_input);

    // Pass data between destinations using safe-args
    ActionStartVisit actionStartVisit = VisitFragmentDirections.actionStartVisit();
    actionStartVisit.setNewVisitTitle(Objects.requireNonNull(newVisitTitle.getText()).toString());

    Navigation.findNavController(view).navigate(actionStartVisit);
  }

  /**
   * Add a click listener to the Start button, to replace current fragment with {@link
   * StartVisitFragment} after explicit permissions check has passed
   */
  private void startNewVisitListener() {
    view.findViewById(R.id.button_start_visit).setOnClickListener(v -> {

      if (checkPlayServices(activity)) {
        boolean isFirstTime = PermissionHelper
            .isFirstTimeAskingPermissions(activity, PERMISSIONS_REQUIRED);

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
    boolean isLocationGranted = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)
        : PermissionHelper.hasLocationPermission(activity);
    boolean isCameraGranted = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(permission.CAMERA)
        : PermissionHelper.hasCameraPermission(activity);
    boolean isStorageGranted = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(permission.WRITE_EXTERNAL_STORAGE)
        : PermissionHelper.hasStoragePermission(activity);

    int permissionCode = ALL_PERMISSIONS_CODE;
    permissionCode -= isLocationGranted ? LOCATION_PERMISSION_CODE : 0;
    permissionCode -= isCameraGranted ? CAMERA_PERMISSION_CODE : 0;
    permissionCode -= isStorageGranted ? STORAGE_PERMISSION_CODE : 0;

    return checkNeverAsk ? ALL_PERMISSIONS_CODE - permissionCode : permissionCode;
  }

  /**
   * @param isFirstTime True if it is the first time the application has asked for these
   * permissions
   * @param permissionGranted Permission code (constant value in {@link PermissionHelper}) of those
   * granted permissions
   * @param permissionsNeverAsked Permission code (constant value in {@link PermissionHelper}) of
   * those never asked permissions
   */
  private void showPermissionRationale(
      boolean isFirstTime,
      int permissionGranted,
      int permissionsNeverAsked
  ) {
    int permissionNotGranted = ALL_PERMISSIONS_CODE - permissionGranted;
    boolean isAllPermissionsAllowed = permissionGranted == ALL_PERMISSIONS_CODE;
    boolean anyNeverAskChecked = !isFirstTime && permissionsNeverAsked != NO_PERMISSIONS_CODE
        && permissionsNeverAsked != permissionGranted;

    int requestCode
        = isFirstTime ? permissionNotGranted
        : isAllPermissionsAllowed ? NO_PERMISSIONS_CODE
            : anyNeverAskChecked ? permissionsNeverAsked | permissionNotGranted
                : permissionNotGranted;

    if (isAllPermissionsAllowed) {
      checkRequiredPermissions(requestCode);
    } else {
      buildDialog(requestCode, anyNeverAskChecked);
    }
  }

  private void buildDialog(int requestCode, boolean anyNeverAskChecked) {
    String message = "To capture and upload photos with location tag, allow Photour access to your "
        + "device's %s. "
        + (anyNeverAskChecked ? "Tap Settings > Permissions, and turn %s." : "");

    AlertDialogHelper alertDialog = new AlertDialogHelper(activity, message);
    alertDialog.initAlertDialog(requestCode);

    if (anyNeverAskChecked) {
      alertDialog.buildSettingsDialog();
    } else {
      alertDialog
          .buildContinueDialog(PERMISSIONS_REQUIRED, () -> checkRequiredPermissions(requestCode));
    }
  }

  /**
   * Check if the applications need to ask for permissions or not
   *
   * @param requestCode The permission request code
   */
  private void checkRequiredPermissions(int requestCode) {
    if (requestCode == NO_PERMISSIONS_CODE) {
      // All permissions required are granted, check if device location is ON
      checkDeviceLocation(activity, this, this::navigateToStartVisit);

    } else {
      // No permissions for location, camera, and storage
      requestPermissions(PERMISSIONS_REQUIRED, requestCode);
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

    PermissionCodeResponse codeResponse = PermissionHelper.CODE_RESPONSE.get(requestCode);

    String message = requestCode == ALL_PERMISSIONS_CODE ? "Required permissions "
        : (codeResponse.getResponseResult());

    message += allPermissionsGranted ? "granted" : "denied";

    if (allPermissionsGranted) {
      checkDeviceLocation(activity, this, this::navigateToStartVisit);
    }

    ToastHelper.tShort(activity, message);
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
      // The user has pressed OK on the Location Settings Dialog
      navigateToStartVisit();
    } else {
      ToastHelper.tShort(activity, "Device location is off (High accuracy mode required)");
    }
  }
}
