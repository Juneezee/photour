package com.android.photour.ui.photos;

import static com.android.photour.helper.PermissionHelper.NO_PERMISSIONS_CODE;
import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.ToastHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fragment for Photos page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosFragment extends Fragment {

  private static final String TAG = "PhotosFragment";

  public static LruCache<String, Bitmap> mRetainedCache;
  private Activity activity;
  private PhotosViewModel photosViewModel;
  private PhotoAdapter photoAdapter;
  private RecyclerView mRecyclerView;
  private FloatingActionButton sortButton;
  private TextView textView;

  /***
   * Finds or create a PhotoFragment using FragmentManager.
   * Used to retain state on rotation
   *
   * @param fm FragmentManager
   * @return PhotoFragment
   */
  public static PhotosFragment findOrCreateRetainFragment(FragmentManager fm) {
    PhotosFragment fragment = (PhotosFragment) fm.findFragmentByTag(TAG);
    if (fragment == null) {
      fragment = new PhotosFragment();
    }
    return fragment;
  }

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
  @SuppressLint("SetTextI18n")
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {

    this.activity = getActivity();
    View view = inflater.inflate(R.layout.fragment_photos, container, false);

    boolean isFirstTime = PermissionHelper
        .isFirstTimeAskingPermissions(activity, permission.WRITE_EXTERNAL_STORAGE);

    int permissionGranted = STORAGE_PERMISSION_CODE - permissionsNotGranted(false);
    int permissionsNeverAsked = permissionsNotGranted(true) - permissionGranted;

    // Display a dialog for permissions explanation
    showPermissionRationale(isFirstTime, permissionGranted, permissionsNeverAsked);

    // Initialise view if has access, else displays a text notice
    textView = view.findViewById(R.id.text_notifications);
    if (PermissionHelper.hasStoragePermission(activity)) {
      initializeRecyclerView(view);
    } else {
      textView.setText("Please Enable Storage Access to use this feature");
    }

    return view;
  }

  /***
   * Initialize recycler view for photos
   *
   *
   * @param root View of PhotosFragment
   */
  @SuppressLint("SetTextI18n")
  private void initializeRecyclerView(View root) {

    //Initialize lists for SectionedGridRecyclerViewAdapter
    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<Uri> uris = new ArrayList<>();

    //If called outside of onCreateView, get view
    if (root == null) {
      root = this.getView();
    }
    textView.setText("");

    //Sets up recycler view and view model
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    mRecyclerView = root.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setHasFixedSize(true);
    int IMAGE_WIDTH = 100;
    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
        PhotosViewModel.calculateNoOfColumns(Objects.requireNonNull(getContext()), IMAGE_WIDTH)));

    //Sets up adapters, photoAdapter is in charge of images, mSectionedAdapter for titles and grid
    photoAdapter = new PhotoAdapter(getContext());
    SectionedGridRecyclerViewAdapter.Section[] dummy =
        new SectionedGridRecyclerViewAdapter.Section[sections.size()];
    SectionedGridRecyclerViewAdapter mSectionedAdapter = new
        SectionedGridRecyclerViewAdapter(Objects.requireNonNull(getActivity()),
        R.layout.fragment_photos_sort, R.id.sorted_title_view, mRecyclerView, photoAdapter);

    //set observer to image list, on calls adapters to reset
    photosViewModel.images.observe(getViewLifecycleOwner(), imageElements -> {
      //resets lists
      sections.clear();
      uris.clear();

      //Prompts text if no images, else load images into lists
      if (imageElements == null || imageElements.size() == 0) {
        textView.setText("No photos! Go make some trips!");
      } else {
        int pos = 0;
        textView.setText("");
        for (ImageElement imageElement : imageElements) {
          sections.add(new SectionedGridRecyclerViewAdapter.Section(pos, imageElement.getTitle()));
          pos += imageElement.getUris().size(); // add number of photos and title
          uris.addAll(imageElement.getUris());
        }
      }

      //parses values into adapters and update view
      photoAdapter.setItems(uris);
      photoAdapter.notifyDataSetChanged();
      mSectionedAdapter.setSections(sections.toArray(dummy));
      mSectionedAdapter.notifyDataSetChanged();
      mRecyclerView.setAdapter(mSectionedAdapter);
    });


    //sets up sort button
    sortButton = root.findViewById(R.id.fab_sort);
    sortButton.setVisibility(View.VISIBLE);
    initializeSortButton();

  }

  /***
   * Initialize sort button.
   * Puts initial drawable and a listener to sort and change drawable
   */
  private void initializeSortButton() {
    sortButton.setImageDrawable(getResources().getDrawable(
        photosViewModel.sortMode == 0 ? R.drawable.ic_calendar : R.drawable.ic_path_white, null));

    sortButton.setOnClickListener(v -> {
      photosViewModel.switchSortMode();
      sortButton.setImageDrawable(getResources().getDrawable(
          photosViewModel.sortMode == 0 ? R.drawable.ic_calendar : R.drawable.ic_path_white, null));
    });
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
   * get the permissions not granted by deny only
   * @return int The permission request code
   */
  private int permissionsNotGranted(boolean checkNeverAsk) {
    boolean storagePermission = checkNeverAsk
        ? !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        : PermissionHelper.hasStoragePermission(activity);

    int permissionCode = STORAGE_PERMISSION_CODE;

    if (storagePermission) {
      // All permissions granted, check if device location is turned on
      permissionCode = NO_PERMISSIONS_CODE;
    }
    return checkNeverAsk ? STORAGE_PERMISSION_CODE - permissionCode : permissionCode;
  }

  private void showPermissionRationale(
      boolean isFirstTime,
      int permissionGranted,
      int permissionsNeverAsked
  ) {
    int permissionNotGranted = STORAGE_PERMISSION_CODE - permissionGranted;
    boolean isAllPermissionsAllowed = permissionGranted == STORAGE_PERMISSION_CODE;
    boolean anyNeverAskChecked = !isFirstTime && permissionsNeverAsked != NO_PERMISSIONS_CODE
        && permissionsNeverAsked != permissionGranted;

    int permissionToRequest
        = isFirstTime ? STORAGE_PERMISSION_CODE
        : isAllPermissionsAllowed ? NO_PERMISSIONS_CODE
            : anyNeverAskChecked ? permissionsNeverAsked | permissionNotGranted
                : permissionNotGranted;

    PermissionHelper.PermissionCodeResponse codeResponse = PermissionHelper.PERMISSIONS_MAP
        .get(permissionToRequest);

    String message = "To access your photos, allow Photour access to your "
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
            PermissionHelper
                .setFirstTimeAskingPermissions(activity, permission.WRITE_EXTERNAL_STORAGE);
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
      requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE}, permissionToRequest);
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

    PermissionHelper.PermissionCodeResponse codeResponse = PermissionHelper.PERMISSIONS_MAP
        .get(requestCode);

    String message = requestCode == STORAGE_PERMISSION_CODE ? "Storage permission "
        : (codeResponse.getResponseResult());
    message += allPermissionsGranted ? "granted" : "denied";

    if (allPermissionsGranted) {
      initializeRecyclerView(null);
    }

    ToastHelper.tShort(activity, message);
  }
}
