package com.photour.ui.visitnew;

import static com.photour.helper.PermissionHelper.ALL_PERMISSIONS_CODE;
import static com.photour.helper.PermissionHelper.CAMERA_PERMISSION_CODE;
import static com.photour.helper.PermissionHelper.LOCATION_PERMISSION_CODE;
import static com.photour.helper.PermissionHelper.NO_PERMISSIONS_CODE;
import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;
import static com.photour.helper.PlayServicesHelper.checkPlayServices;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.photour.databinding.FragmentVisitBinding;
import com.photour.helper.AlertDialogHelper;
import com.photour.helper.LocationHelper;
import com.photour.helper.PermissionHelper;
import com.photour.helper.ToastHelper;
import com.photour.ui.visitnew.NewVisitFragmentDirections.ActionStartVisit;

/**
 * Fragment for New Visit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class NewVisitFragment extends Fragment {

  public static final int REQUEST_CHECK_SETTINGS = 214;
  private static final String[] PERMISSIONS_REQUIRED = {
      permission.ACCESS_FINE_LOCATION,
      permission.CAMERA,
      permission.WRITE_EXTERNAL_STORAGE
  };

  private PermissionHelper permissionHelper;

  private Activity activity;
  private FragmentVisitBinding binding;

  /**
   * Called to do initial creation of a fragment.  This is called after {@link #onAttach(Activity)}
   * and before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   * is the state.
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = getActivity();
    permissionHelper = new PermissionHelper(activity, this, PERMISSIONS_REQUIRED);
  }

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

    binding = FragmentVisitBinding.inflate(inflater, container, false);
    binding.setListener(this);

    return binding.getRoot();
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
    binding.textclock.setFormat24Hour("EEEE, dd MMMM yyyy\n\nHH:mm:ss");
    binding.textclock.setFormat12Hour("EEEE, dd MMMM yyyy\n\nh:mm:ss a");
  }

  /**
   * Add a click listener to the Start button, to replace current fragment with {@link
   * StartVisitFragment} after explicit permissions check has passed
   */
  public void onStartClick() {
    if (checkPlayServices(activity)) {
      boolean isFirstTime = permissionHelper.isFirstTimeAskingPermissions();
      int permissionGranted = ALL_PERMISSIONS_CODE - permissionsNotGranted();
      int permissionsNeverAsked = permissionsNeverAsked();

      Log.d("Perm", "Permission granted " + permissionGranted);
      Log.d("Perm", "Permission not granted " + (ALL_PERMISSIONS_CODE - permissionGranted));
      Log.d("Perm", "Permission never asked " + permissionsNeverAsked);

      // Display a dialog for permissions explanation
      showPermissionRationale(isFirstTime, permissionGranted, permissionsNeverAsked);

    } else {
      ToastHelper.tShort(activity, "Play services not available");
    }
  }

  /**
   * Get the permissions not granted by user due to denying, or
   *
   * @return int The permission request code
   */
  private int permissionsNotGranted() {
    boolean isLocationGranted = permissionHelper.hasLocationPermission();
    boolean isCameraGranted = permissionHelper.hasCameraPermission();
    boolean isStorageGranted = permissionHelper.hasStoragePermission();

    int permissionCode = ALL_PERMISSIONS_CODE;

    permissionCode -= isLocationGranted ? LOCATION_PERMISSION_CODE : 0;
    permissionCode -= isCameraGranted ? CAMERA_PERMISSION_CODE : 0;
    permissionCode -= isStorageGranted ? STORAGE_PERMISSION_CODE : 0;

    return permissionCode;
  }

  /**
   * Get the permissions not granted by user due to "Never ask again"
   *
   * shouldShowRequestPermissionRationale() returns true if the user has previously denied the
   * request, and returns false if first time, orf a permission is allowed, or a user has denied a
   * permission and selected the Don't ask again option
   *
   * @return int The permission request code
   */
  private int permissionsNeverAsked() {
    boolean isLocationNeverAsked =
        !permissionHelper.isFirstTimeAskingPermission(permission.ACCESS_FINE_LOCATION)
            && !shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION);
    boolean isCameraNeverAsked = !permissionHelper.isFirstTimeAskingPermission(permission.CAMERA)
        && !shouldShowRequestPermissionRationale(permission.CAMERA);
    boolean isStorageNeverAsked =
        !permissionHelper.isFirstTimeAskingPermission(permission.WRITE_EXTERNAL_STORAGE)
            && !shouldShowRequestPermissionRationale(permission.WRITE_EXTERNAL_STORAGE);

    int permissionCode = NO_PERMISSIONS_CODE;
    permissionCode += isLocationNeverAsked ? LOCATION_PERMISSION_CODE : 0;
    permissionCode += isCameraNeverAsked ? CAMERA_PERMISSION_CODE : 0;
    permissionCode += isStorageNeverAsked ? STORAGE_PERMISSION_CODE : 0;

    return permissionCode;
  }

  /**
   * Show rationale when requesting the permissions required
   *
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
            : anyNeverAskChecked ?
                (permissionsNeverAsked | permissionNotGranted) - permissionGranted
                : permissionNotGranted;

    permissionHelper.setRequestCode(requestCode);

    if (isAllPermissionsAllowed) {
      // All permissions required are granted, check whether device location is ON
      permissionHelper.checkRequiredPermissions(this::checkDeviceLocation);
    } else {
      buildDialog(requestCode, anyNeverAskChecked);
    }
  }

  /**
   * Build an AlertDialog to display the rationale
   *
   * @param requestCode The permission request code
   * @param anyNeverAskChecked True if the user has selected "Never ask again" for at least one
   * permission
   */
  private void buildDialog(int requestCode, boolean anyNeverAskChecked) {
    String message = "To capture and upload photos with location tag, allow Photour access to your "
        + "device's %s. "
        + (anyNeverAskChecked ? "Tap Settings > Permissions, and turn %s." : "");

    AlertDialogHelper alertDialog = new AlertDialogHelper(activity, message);
    alertDialog.initAlertDialog(requestCode);

    if (anyNeverAskChecked) {
      alertDialog.buildSettingsDialog();
    } else {
      alertDialog.buildContinueDialog(permissionHelper, this::checkDeviceLocation);
    }
  }

  /**
   * If all permissions required (location, camera, storage) are granted, then check if device
   * location is ON
   */
  private void checkDeviceLocation() {
    LocationHelper.checkDeviceLocation(activity, this, this::navigateToStartVisit);
  }

  /**
   * Navigate to the start visit fragment. Condition: Device location turned on, location
   * permission, camera permission, and storage permission granted
   */
  private void navigateToStartVisit() {
    // Pass data between destinations using safe-args
    ActionStartVisit actionStartVisit = NewVisitFragmentDirections.actionStartVisit();
    Editable newVisitTitle = binding.newVisitTitleInput.getText();

    if (newVisitTitle != null && !newVisitTitle.toString().trim().isEmpty()) {
      actionStartVisit.setNewVisitTitle(newVisitTitle.toString().trim());
    }

    Navigation.findNavController(binding.getRoot()).navigate(actionStartVisit);
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

    permissionHelper.onRequestPermissionsResult(grantResults, this::checkDeviceLocation);
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
