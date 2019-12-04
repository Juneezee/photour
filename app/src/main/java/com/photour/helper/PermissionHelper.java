package com.photour.helper;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.photour.R;

/**
 * Helper class for handling runtime permissions
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PermissionHelper {

  private Activity activity;
  private Fragment fragment;
  private String[] permissions;
  private int requestCode;

  public static final int NO_PERMISSIONS_CODE = 0;
  public static final int ALL_PERMISSIONS_CODE = 111;
  public static final int LOCATION_PERMISSION_CODE = 100;
  public static final int CAMERA_PERMISSION_CODE = 10;
  public static final int STORAGE_PERMISSION_CODE = 1;
  private static final int LC_PERMISSION_CODE = 110; // LC: Location and Camera
  private static final int LS_PERMISSION_CODE = 101; // LS: Location and Storage
  private static final int CS_PERMISSION_CODE = 11; // CS: Camera and Storage

  static final SparseArray<PermissionCodeResponse> CODE_RESPONSE = new SparseArray<PermissionCodeResponse>() {
    {
      append(ALL_PERMISSIONS_CODE, new PermissionCodeResponse(R.layout.dialog_permission_all,
          "location, camera, and storage ", "Location ON, Camera ON, and Storage ON"));
      append(LOCATION_PERMISSION_CODE,
          new PermissionCodeResponse(R.layout.dialog_permission_location,
              "location ", "Location ON"));
      append(CAMERA_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_camera,
          "camera ", "Camera ON"));
      append(STORAGE_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_storage,
          "storage ", "Storage ON"));
      append(LC_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_lc,
          "location and camera ", "Location ON and Camera ON"));
      append(LS_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_ls,
          "location and storage ", "Location ON and Storage ON"));
      append(CS_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_cs,
          "camera and storage ", "Camera ON and Storage ON"));
      append(NO_PERMISSIONS_CODE, new PermissionCodeResponse(R.layout.dialog_permission_all,
          "", ""));
    }
  };

  /**
   * Constructor for PermissionHelper class
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param fragment The fragment that is requesting the permissions
   * @param permissions The String array of permissions to check
   */
  public PermissionHelper(Activity activity, Fragment fragment, String[] permissions) {
    this.activity = activity;
    this.fragment = fragment;
    this.permissions = permissions;
  }

  /**
   * Set the permission request code
   *
   * @param requestCode The permission request code
   */
  public void setRequestCode(int requestCode) {
    this.requestCode = requestCode;
  }

  /**
   * 1. Check if device version is Marshmallow (API 23) and above. Used in deciding to ask runtime
   * permission
   *
   * 2. Check if the specific permission has been granted or not
   *
   * @param permission The permission to check
   * @return boolean True if the current device is Android M or above and the specific permission
   * has been granted
   */
  private boolean shouldAskPermission(String permission) {
    return activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Check for WRITE_EXTERNAL_STORAGE permission
   *
   * @param listener A {@link PermissionAskListener} instance for callback
   */
  public void checkStoragePermission(PermissionsResultListener listener) {
    checkPermission(permission.WRITE_EXTERNAL_STORAGE, new PermissionAskListener() {
      @Override
      public void onPermissionAsk() {
        buildDialog(false, listener);
      }

      @Override
      public void onPermissionDisabled() {
        buildDialog(true, listener);
      }

      @Override
      public void onPermissionGranted() {
        listener.onPermissionsGranted();
      }
    });
  }

  /**
   * Build an AlertDialog to display the rationale for a WRITE_EXTERNAL_PERMISSION permission only
   *
   * @param isSettingsDialog True to show "Settings" (brings user to application details setting,
   * only when the permission is set as "Never ask again") instead of "Continue"
   */
  private void buildDialog(boolean isSettingsDialog, PermissionsResultListener listener) {
    String message = "To access your photos, allow Photour access to your device's storage. "
        + (isSettingsDialog ? "Tap Settings > Permissions, and turn Storage ON." : "");

    AlertDialogHelper alertDialogHelper = new AlertDialogHelper(activity, message);
    alertDialogHelper.initAlertDialog(STORAGE_PERMISSION_CODE);
    alertDialogHelper.initBuilder();

    if (isSettingsDialog) {
      alertDialogHelper.buildSettingsDialog();
    } else {
      alertDialogHelper.buildContinueDialog(this, listener);
    }
  }

  /**
   * 1. App launched first time,
   *
   * 2. App launched before, and the user had denied the permission in previous launches
   *
   * 2A. The user denied permission earlier WITHOUT checking "Never ask again".
   *
   * 2B. The user denied permission earlier WITH checking "Never ask again".
   *
   * @param permission The permission to check
   * @param listener A {@link PermissionAskListener} instance for callback
   */
  private void checkPermission(String permission, PermissionAskListener listener) {
    /*
     * If permission is not granted
     */
    if (shouldAskPermission(permission)) {
      /*
       * If permission denied previously
       * */
      if (activity.shouldShowRequestPermissionRationale(permission)) {
        listener.onPermissionAsk();
      } else {
        /*
         * Permission denied or first time requested
         */
        if (isFirstTimeAskingPermissions()) {
          listener.onPermissionAsk();
        } else {
          /*
           * Handle the feature without permission or ask user to manually allow permission
           */
          listener.onPermissionDisabled();
        }
      }
    } else {
      listener.onPermissionGranted();
    }
  }

  /**
   * Check all the required permissions specified in <var>permissions</var>
   *
   * @param listener A {@link PermissionAskListener} instance for callback
   */
  public void checkRequiredPermissions(PermissionsResultListener listener) {
    if (requestCode == NO_PERMISSIONS_CODE) {
      // All permissions required are granted
      listener.onPermissionsGranted();
    } else {
      // The permissions required are not granted, or granted partially
      fragment.requestPermissions(permissions, requestCode);
    }
  }

  /**
   * Callback helper for the result from requesting permissions.
   *
   * @param grantResults The grant results for the corresponding permissions which is either {@link
   * android.content.pm.PackageManager#PERMISSION_GRANTED} or {@link android.content.pm.PackageManager#PERMISSION_DENIED}.
   * Never null.
   * @param listener A {@link PermissionsResultListener} instance for callback
   */
  public void onRequestPermissionsResult(
      @NonNull int[] grantResults,
      PermissionsResultListener listener
  ) {
    boolean allPermissionsGranted = true;

    for (int grantResult : grantResults) {
      allPermissionsGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
    }

    PermissionCodeResponse codeResponse = CODE_RESPONSE.get(requestCode);

    String message = requestCode == ALL_PERMISSIONS_CODE ? "Required permissions "
        : codeResponse.getResponseResult();
    message += allPermissionsGranted ? "granted" : "denied";

    if (allPermissionsGranted) {
      try {
        listener.onPermissionsGranted();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    ToastHelper.tShort(activity, message);
  }

  /**
   * Check if the application is requesting the specific permission for the first time
   *
   * @return boolean True if the application is asking the specific permission for the first time
   */
  public boolean isFirstTimeAskingPermission(String permission) {
    return activity.getPreferences(MODE_PRIVATE).getBoolean(permission, true);
  }

  /**
   * Check if the application is requesting the array of permissions for the first time
   *
   * @return boolean True if the application is asking the array of permissions for the first time
   */
  public boolean isFirstTimeAskingPermissions() {
    boolean isFirstTime = true;

    for (String permission : permissions) {
      isFirstTime &= activity.getPreferences(MODE_PRIVATE).getBoolean(permission, true);
    }

    return isFirstTime;
  }

  /**
   * Set the "firstTime" attribute of the array of permissions to false in MainActivity.xml
   */
  void setFirstTimeAskingPermissions() {
    SharedPreferences sharedPreference = activity.getPreferences(MODE_PRIVATE);

    for (String permission : permissions) {
      if (isFirstTimeAskingPermission(permission)) {
        sharedPreference.edit().putBoolean(permission, false).apply();
      }
    }
  }

  /**
   * Static method to check if the application has access to location
   *
   * @param context The context of the application
   * @return {@code true} if the application has location permission
   */
  public static boolean hasLocationPermission(Context context) {
    return context.checkSelfPermission(permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Check if the application has access to location
   *
   * @return boolean {@code true} if the application has location permission
   */
  public boolean hasLocationPermission() {
    return !shouldAskPermission(permission.ACCESS_FINE_LOCATION);
  }

  /**
   * Static method to check if the application has access to camera
   *
   * @param context The context of the application
   * @return {@code true} if the application has camera permission
   */
  public static boolean hasCameraPermission(Context context) {
    return context.checkSelfPermission(permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Check if the application has access to camera
   *
   * @return boolean {@code true} if the application has camera permission
   */
  public boolean hasCameraPermission() {
    return !shouldAskPermission(permission.CAMERA);
  }

  /**
   * Static method to check if the application has access to storage
   *
   * @param context The context of the application
   * @return {@code true} if the application has storage permission
   */
  public static boolean hasStoragePermission(Context context) {
    return context.checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Check if the application has access to storage
   *
   * @return boolean {@code true} if the application has storage permission
   */
  public boolean hasStoragePermission() {
    return !shouldAskPermission(permission.WRITE_EXTERNAL_STORAGE);
  }

  /**
   * An interface to handle callbacks on various cases in checking permission
   *
   * 1. Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
   * If permission is already granted, onPermissionGranted() would be called.
   *
   * 2. Above M, if the permission is being asked first time onPermissionAsk() would be called.
   *
   * 3. Above M, if the permission is previously asked but not granted,
   * onPermissionPreviouslyDenied() would be called.
   *
   * 4. Above M, if the permission is disabled by device policy or the user checked "Never ask
   * again" check box on previous request permission, onPermissionDisabled() would be called.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public interface PermissionAskListener {

    /**
     * Callback to ask permission
     */
    void onPermissionAsk();

    /**
     * Callback on permission "Never show again" checked and denied
     */
    void onPermissionDisabled();

    /**
     * Callback on permission granted
     */
    void onPermissionGranted();
  }

  /**
   * An interface to handle the success event of onRequestPermissionsResult
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public interface PermissionsResultListener {

    /**
     * Callback on all permissions granted
     */
    void onPermissionsGranted();
  }

  /**
   * Static class for handling the rationale strings of permission request code
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public static class PermissionCodeResponse {

    private int layout;
    private String rationaleName, rationaleNameOn;

    /**
     * Constructor for {@link PermissionCodeResponse}
     *
     * @param layout Layout to use in the alert dialog title
     * @param rationaleName The name of the permissions to show on rationale dialog
     * @param rationaleNameOn The name of the permissions + "ON" to show on rationale dialog (e.g.
     * Location ON, Camera ON,...)
     */
    PermissionCodeResponse(int layout, String rationaleName, String rationaleNameOn) {
      this.layout = layout;
      this.rationaleName = rationaleName;
      this.rationaleNameOn = rationaleNameOn;
    }

    /**
     * Get the layout to use for the permission request code
     *
     * @return int The layout to use in the alert dialog title
     */
    public int getLayout() {
      return layout;
    }

    /**
     * Get the name of the permissions to show on the rationale dialog
     *
     * @return String The name of the permissions to show on the rationale dialog
     */
    String getRationaleName() {
      return rationaleName;
    }

    /**
     * Get the name of th permissions + "ON" to show on the rationale dialog
     *
     * @return String The name of the permissions + "ON" to show on the rationale dialog
     */
    String getRationaleNameOn() {
      return rationaleNameOn;
    }

    /**
     * Get the response result to show on Toast
     *
     * @return String The name of the permission with first letter capitalised to show on Toast
     */
    String getResponseResult() {
      return rationaleName.substring(0, 1).toUpperCase() + rationaleName.substring(1);
    }
  }
}
