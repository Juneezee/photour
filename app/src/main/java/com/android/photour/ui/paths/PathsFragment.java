package com.android.photour.ui.paths;

import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest.permission;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.android.photour.R;
import com.android.photour.helper.AlertDialogHelper;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.PermissionHelper.PermissionAskListener;

public class PathsFragment extends Fragment {

  private static final String[] PERMISSIONS_REQUIRED = {
      permission.WRITE_EXTERNAL_STORAGE
  };

  private PermissionHelper permissionHelper;

  private Activity activity;

  /**
   * Called to have the fragment instantiate its user interface view. Permission for storage access
   * is handled here
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
      Bundle savedInstanceState
  ) {

    this.activity = getActivity();

    PathsViewModel pathsViewModel = new ViewModelProvider(this).get(PathsViewModel.class);
    View root = inflater.inflate(R.layout.fragment_paths, container, false);
    final TextView textView = root.findViewById(R.id.text_notifications);
    pathsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    return root;
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

    permissionHelper = new PermissionHelper(activity, this, PERMISSIONS_REQUIRED);
    permissionHelper.setRequestCode(STORAGE_PERMISSION_CODE);

    // Check if storage permission is granted or not
    permissionHelper.checkStoragePermission(new PermissionAskListener() {
      @Override
      public void onPermissionAsk() {
        buildDialog(false);
      }

      @Override
      public void onPermissionPreviouslyDenied() {
        buildDialog(false);
      }

      @Override
      public void onPermissionDisabled() {
        buildDialog(true);
      }

      @Override
      public void onPermissionGranted() {

      }
    });
  }

  /**
   * Build an AlertDialog to display the rationale
   *
   * @param isSettingsDialog True to show "Settings" (brings user to application details setting,
   * only when the permission is set as "Never ask again") instead of "Continue"
   */
  private void buildDialog(boolean isSettingsDialog) {
    String message =
        "To access your paths and photos, allow Photour access to your device's storage. "
            + (isSettingsDialog ? "Tap Settings > Permissions, and turn Storage ON." : "");

    AlertDialogHelper alertDialogHelper = new AlertDialogHelper(activity, message);
    alertDialogHelper.initAlertDialog(STORAGE_PERMISSION_CODE);
    alertDialogHelper.initBuilder();

    if (isSettingsDialog) {
      alertDialogHelper.buildSettingsDialog();
    } else {
      alertDialogHelper.buildContinueDialog(permissionHelper, () -> {
      });
    }
  }
}
