package com.android.photour.helper;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest.permission;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.SparseArray;
import com.android.photour.R;

/**
 * Helper class for handling runtime permissions
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PermissionHelper {

  public static final int NO_PERMISSIONS_CODE = 0;
  public static final int ALL_PERMISSIONS_CODE = 111;
  public static final int LOCATION_PERMISSION_CODE = 100;
  public static final int CAMERA_PERMISSION_CODE = 10;
  public static final int STORAGE_PERMISSION_CODE = 1;
  public static final int LC_PERMISSION_CODE = 110; // LC: Location and Camera
  public static final int LS_PERMISSION_CODE = 101; // LS: Location and Storage
  public static final int CS_PERMISSION_CODE = 11; // CS: Camera and Storage

  public static final SparseArray<PermissionCodeResponse> PERMISSIONS_MAP = new SparseArray<PermissionCodeResponse>() {
    {
      append(ALL_PERMISSIONS_CODE, new PermissionCodeResponse(R.layout.dialog_permission_all,
          "location, camera, and storage", "location ON, camera ON, and storage ON"));
      append(LOCATION_PERMISSION_CODE,
          new PermissionCodeResponse(R.layout.dialog_permission_location,
              "location ", "location ON"));
      append(CAMERA_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_camera,
          "camera", "camera ON"));
      append(STORAGE_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_storage,
          "storage", "storage ON"));
      append(LC_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_lc,
          "location and camera", "location ON and camera ON"));
      append(LS_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_ls,
          "location and storage", "location ON and storage ON"));
      append(CS_PERMISSION_CODE, new PermissionCodeResponse(R.layout.dialog_permission_cs,
          "camera and storage", "camera ON and storage ON"));
      append(NO_PERMISSIONS_CODE, new PermissionCodeResponse(R.layout.dialog_permission_all,
          "", ""));
    }
  };

  /**
   * 1. Check if device version is Marshmallow (API 23) and above. Used in deciding to ask runtime
   * permission
   *
   * 2. Check if the specific permission has been granted or not
   *
   * @param activity The current activity
   * @param permission The permission to check
   * @return boolean True if the current device is Android M or above and the specific permission
   * has been granted
   */
  private static boolean shouldAskPermission(Activity activity, String permission) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
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
   * @param activity The current activity
   * @param permission The permission to check
   * @param listener A {@link PermissionAskListener} instance for callback
   */
  public static void checkPermission(Activity activity, String permission,
      PermissionAskListener listener) {
    /*
     * If permission is not granted
     */
    if (shouldAskPermission(activity, permission)) {
      /*
       * If permission denied previously
       * */
      if (activity.shouldShowRequestPermissionRationale(permission)) {
        listener.onPermissionPreviouslyDenied();
      } else {
        /*
         * Permission denied or first time requested
         */
        if (isFirstTimeAskingPermission(activity, permission)) {
          setFirstTimeAskingPermission(activity, permission);
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
   * Check if the application is requesting a permission for the first time
   *
   * @param activity The current activity
   * @param permission The array of permissions to check
   * @return boolean True if the application is asking the array of permissions for the first time
   */
  private static boolean isFirstTimeAskingPermission(Activity activity, String permission) {
    return activity.getPreferences(MODE_PRIVATE).getBoolean(permission, true);
  }

  /**
   * Set the "firstTime" attribute of the permission to false in shared_preferences.xml
   *
   * @param activity The current activity
   * @param permission The array of permissions to set
   */
  private static void setFirstTimeAskingPermission(Activity activity, String permission) {
    SharedPreferences sharedPreference = activity.getPreferences(MODE_PRIVATE);
    sharedPreference.edit().putBoolean(permission, false).apply();
  }

  /**
   * Check if the application is requesting the array of permissions for the first time
   *
   * @param activity The current activity
   * @param permissions The array of permissions to check
   * @return boolean True if the application is asking the array of permissions for the first time
   */
  public static boolean isFirstTimeAskingPermissions(Activity activity, String... permissions) {
    boolean isFirstTime = true;

    for (String permission : permissions) {
      isFirstTime &= activity.getPreferences(MODE_PRIVATE).getBoolean(permission, true);
    }

    return isFirstTime;
  }

  /**
   * Set the "firstTime" attribute of the array of permissions to false in shared_preferences.xml
   *
   * @param activity The current activity
   * @param permissions The array of permissions to set
   */
  public static void setFirstTimeAskingPermissions(Activity activity, String... permissions) {
    SharedPreferences sharedPreference = activity.getPreferences(MODE_PRIVATE);

    for (String permission : permissions) {
      sharedPreference.edit().putBoolean(permission, false).apply();
    }
  }

  /**
   * Check if the application has access to location
   *
   * @param activity The current activity
   * @return boolean True if the application has location permission
   */
  public static boolean hasLocationPermission(Activity activity) {
    return !shouldAskPermission(activity, permission.ACCESS_FINE_LOCATION);
  }

  /**
   * Check if the application has access to camera
   *
   * @param activity The current activity
   * @return boolean True if the application has camera permission
   */
  public static boolean hasCameraPermission(Activity activity) {
    return !shouldAskPermission(activity, permission.CAMERA);
  }

  /**
   * Check if the application has access to storage
   *
   * @param activity The current activity
   * @return boolean True if the application has storage permission
   */
  public static boolean hasStoragePermission(Activity activity) {
    return !shouldAskPermission(activity, permission.WRITE_EXTERNAL_STORAGE);
  }

  /**
   * Callback on various cases on checking permission
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
     * Callback on permission denied
     */
    void onPermissionPreviouslyDenied();

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
   *
   */
  public static class PermissionCodeResponse {

    private int layout;
    private String rationaleName, rationaleNameOn;

    PermissionCodeResponse(int layout, String rationaleName, String rationaleNameOn) {
      this.layout = layout;
      this.rationaleName = rationaleName;
      this.rationaleNameOn = rationaleNameOn;
    }

    public int getLayout() {
      return layout;
    }

    public String getRationaleName() {
      return rationaleName;
    }

    public String getRationaleNameOn() {
      return rationaleNameOn;
    }

    public String getResponseResult() {
      return rationaleName.substring(0, 1).toUpperCase() + rationaleName.substring(1);
    }
  }
}
