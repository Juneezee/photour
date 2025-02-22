package com.photour.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import com.photour.helper.PermissionHelper.PermissionCodeResponse;
import com.photour.helper.PermissionHelper.PermissionsResultListener;

/**
 * Helper class for creating AlertDialog that shows rationale for permissions request
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class AlertDialogHelper {

  private Activity activity;
  private String message;
  private int titleLayout;

  private AlertDialog.Builder builder;

  /**
   * Constructor for {@link AlertDialogHelper}
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param message The message to show on the AlertDialog (unformatted)
   */
  public AlertDialogHelper(Activity activity, String message) {
    this.activity = activity;
    this.message = message;
  }

  /**
   * Create a delete visit confirmation dialog
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param message The message of the alert dialog
   * @param listener An {@link AlertDialogListener} instance for callback
   */
  private static void createConfirmationDialog(
      Activity activity, String message, AlertDialogListener listener
  ) {
    new AlertDialog.Builder(activity)
        .setMessage(message)
        .setPositiveButton("Yes", (dialog, which) -> listener.onPositive())
        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
        .setCancelable(false)
        .show();
  }

  /**
   * Create an exit confirmation dialog
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param listener An {@link AlertDialogListener} instance for callback
   */
  public static void createExitConfirmationDialog(Activity activity, AlertDialogListener listener) {
    createConfirmationDialog(activity, "Are you sure you want to exit?", listener);
  }

  /**
   * Create a delete visit confirmation dialog
   *
   * @param activity The {@link com.photour.MainActivity}
   * @param listener An {@link AlertDialogListener} instance for callback
   */
  public static void createDeleteConfirmationDialog(
      Activity activity, AlertDialogListener listener
  ) {
    createConfirmationDialog(
        activity,
        "Deleting a visit is permanent. Photos will remain in your device memory.",
        listener);
  }

  /**
   * Initialise the alert dialog with title layout and formatted message
   *
   * @param requestCode The permission request code
   */
  public void initAlertDialog(int requestCode) {
    PermissionCodeResponse codeResponse = PermissionHelper.CODE_RESPONSE.get(requestCode);
    titleLayout = codeResponse.getLayout();

    message = String
        .format(message, codeResponse.getRationaleName(), codeResponse.getRationaleNameOn());

    initBuilder();
  }

  /**
   * Initialise the builder, settings necessary elements
   */
  void initBuilder() {
    builder = new Builder(activity);
    builder.setMessage(message);
    builder.setCustomTitle(activity.getLayoutInflater().inflate(titleLayout, null));
    builder.setNegativeButton("NOT NOW", (dialog, which) -> dialog.dismiss());
  }

  /**
   * The user has checked "Never ask again" for any permissions. The dialog should show Settings as
   * positive button that brings the user to application settings page to enable permissions
   */
  public void buildSettingsDialog() {
    builder.setPositiveButton("SETTINGS", (dialog, which) -> activity.startActivity(new Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        new Uri.Builder().scheme("package").opaquePart(activity.getPackageName()).build()
    )));

    builder.create().show();
  }

  /**
   * The user has not checked "Never ask again" for any permissions. The dialog should show Continue
   * as positive button that keeps asking the user to grant permissions.
   *
   * When the OK button is clicked. onSuccess listener is activated
   *
   * @param permissionHelper A {@link PermissionHelper} instance
   * @param listener A {@link PermissionsResultListener} instance for callback
   */
  public void buildContinueDialog(
      PermissionHelper permissionHelper,
      PermissionsResultListener listener
  ) {
    builder.setPositiveButton("CONTINUE", (dialog, which) -> {
      permissionHelper.setFirstTimeAskingPermissions();
      permissionHelper.checkRequiredPermissions(listener);
    });
    builder.create().show();
  }

  /**
   * An interface to handle the button click of AlertDialog
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public interface AlertDialogListener {

    /**
     * Callback on positive button click
     */
    void onPositive();
  }
}
