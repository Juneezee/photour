package com.android.photour.ui.photos;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.photour.ImageElement;
import com.android.photour.R;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.android.photour.helper.LocationServicesHelper.checkDeviceLocation;
import static com.android.photour.helper.PermissionHelper.ALL_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.CAMERA_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.CS_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LC_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LOCATION_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.LS_PERMISSION_CODE;
import static com.android.photour.helper.PermissionHelper.NO_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

public class PhotosFragment extends Fragment {

  public static LruCache<String, Bitmap> mRetainedCache;
  private Activity activity;

  private PhotosViewModel photosViewModel;
  private PhotoAdapter photoAdapter;
  private RecyclerView mRecyclerView;
  private FloatingActionButton sortButton;

  private static final String TAG = "PhotosFragment";
  private final int IMAGE_WIDTH = 100;
  private static final int PLAY_SERVICES_ERROR_CODE = 9002;

  private static final String[] ALL_PERMISSIONS_REQUIRED = {
          Manifest.permission.READ_EXTERNAL_STORAGE
  };

  public static PhotosFragment findOrCreateRetainFragment(FragmentManager fm) {
    PhotosFragment fragment = (PhotosFragment) fm.findFragmentByTag(TAG);
    if (fragment == null) {
      fragment = new PhotosFragment();
    }
    return fragment;
  }

//  public void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setRetainInstance(true);
//  }

  public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState) {
    this.activity = getActivity();
    View root = inflater.inflate(R.layout.fragment_photos, container, false);

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

    if (PermissionHelper.hasStoragePermission(activity)) {
      initializeRecyclerView(root);
    }

    return root;
  }

  private void initializeRecyclerView(View root) {
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<Uri> uris = new ArrayList<>();
    if (root == null) {
      root = this.getView();
    }
    

    mRecyclerView = root.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
            PhotosViewModel.calculateNoOfColumns(Objects.requireNonNull(getContext()), IMAGE_WIDTH)));

    photoAdapter = new PhotoAdapter(getContext());
    SectionedGridRecyclerViewAdapter.Section[] dummy =
            new SectionedGridRecyclerViewAdapter.Section[sections.size()];
    SectionedGridRecyclerViewAdapter mSectionedAdapter = new
            SectionedGridRecyclerViewAdapter(getActivity(),
            R.layout.fragment_photos_sort, R.id.sorted_title_view, mRecyclerView, photoAdapter);

    photosViewModel.images.observe(getViewLifecycleOwner(), imageElements -> {
      sections.clear();
      uris.clear();

      int pos = 0;
      for (ImageElement imageElement : imageElements) {
        sections.add(new SectionedGridRecyclerViewAdapter.Section(pos, imageElement.getTitle()));
        pos += imageElement.getUris().size(); // add number of photos and title
        uris.addAll(imageElement.getUris());
      }
      photoAdapter.setItems(uris);
      photoAdapter.notifyDataSetChanged();
      mSectionedAdapter.notifyDataSetChanged();
      mSectionedAdapter.setSections(sections.toArray(dummy));
      mRecyclerView.setAdapter(mSectionedAdapter);
    });

    sortButton = root.findViewById(R.id.fab_sort);
    sortButton.setVisibility(View.VISIBLE);
    initializeSortButton();

  }

  private void initializeSortButton() {
    if (photosViewModel.sortMode == 0) {
      sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar, null));
    } else {
      sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map, null));
    }
    sortButton.setOnClickListener(v -> {
      photosViewModel.switchSortMode();
      if (photosViewModel.sortMode == 0) {
        sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar, null));
      } else {
        sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map, null));
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
   * <p>
   * shouldShowRequestPermissionRationale() returns true if the user has previously denied the
   * request, and returns false if first time, or a permission is allowed, or a user has denied a
   * permission and selected the Don't ask again option
   *
   * @param checkNeverAsk True if to get the permissions that are set as "Never ask again", False to
   *                      get the permissions not granted by deny only
   * @return int The permission request code
   */
  private int permissionsNotGranted(boolean checkNeverAsk) {
    boolean storagePermission = checkNeverAsk
            ? !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            : PermissionHelper.hasStoragePermission(activity);

    int permissionCode = ALL_PERMISSIONS_CODE;

    if (storagePermission) {
      // All permissions granted, check if device location is turned on
      permissionCode = NO_PERMISSIONS_CODE;
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
            : anyNeverAskChecked ? permissionsNeverAsked | permissionNotGranted
            : permissionNotGranted;

    PermissionHelper.PermissionCodeResponse codeResponse = PermissionHelper.PERMISSIONS_MAP.get(permissionToRequest);

    String message = "To capture and upload photos with location tag, allow Photour access to your "
            + "device's %s. "
            + (anyNeverAskChecked ? "Tap Settings > Permissions, and turn %s." : "");

    message = String
            .format(message, codeResponse.getRationaleName(), codeResponse.getRationaleNameOn());

    if (isAllPermissionsAllowed) {
      checkRequiredPermissions(permissionToRequest);
    } else {
      int titleLayout = codeResponse.getLayout();

      buildDialog(R.layout.dialog_permission_storage, message, permissionToRequest, anyNeverAskChecked);
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
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
    if (permissionToRequest != NO_PERMISSIONS_CODE) {
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

    PermissionHelper.PermissionCodeResponse codeResponse = PermissionHelper.PERMISSIONS_MAP.get(requestCode);

    String result = allPermissionsGranted ? "granted" : "denied";

    String message
            = requestCode == ALL_PERMISSIONS_CODE
            ? "Required permissions "
            : (codeResponse.getResponseResult());
    message += result;

    if (allPermissionsGranted) {
      initializeRecyclerView(null);
    }

    ToastHelper.tShort(activity, message);
  }
}
