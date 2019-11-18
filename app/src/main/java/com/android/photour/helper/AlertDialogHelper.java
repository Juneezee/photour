package com.android.photour.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import com.android.photour.helper.PermissionHelper.PermissionCodeResponse;

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
   * @param activity The current activity
   * @param message The message to show on the AlertDialog (unformat)
   */
  public AlertDialogHelper(Activity activity, String message) {
    this.activity = activity;
    this.message = message;
  }

  /**
   * Initialise the alert dialog with title layout and formatted message
   *
   * @param permissionToRequest The permissions to request
   */
  public void initAlertDialog(int permissionToRequest) {
    PermissionCodeResponse codeResponse = PermissionHelper.CODE_RESPONSE.get(permissionToRequest);
    titleLayout = codeResponse.getLayout();

    message = String
        .format(message, codeResponse.getRationaleName(), codeResponse.getRationaleNameOn());

    initBuilder();
  }

  /**
   * Initialise the builder, settings necessary elements
   */
  private void initBuilder() {
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
   * @param permissionsRequested The permissions requested
   * @param listener A {@link AlertDialogListener} instance for callback
   */
  public void buildContinueDialog(String[] permissionsRequested, AlertDialogListener listener) {
    builder.setPositiveButton("CONTINUE", (dialog, which) -> {
      PermissionHelper.setFirstTimeAskingPermissions(activity, permissionsRequested);
      listener.onSuccess();
    });
    builder.create().show();
  }

  /**
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public interface AlertDialogListener {

    /**
     * Callback on positive button clicked by user
     */
    void onSuccess();
  }
}
