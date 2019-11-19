package com.android.photour.ui.photos;

import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;
import com.android.photour.helper.AlertDialogHelper;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.PermissionHelper.PermissionAskListener;
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

  private static final String[] PERMISSIONS_REQUIRED = {
      permission.WRITE_EXTERNAL_STORAGE
  };

  private PermissionHelper permissionHelper;

  private PhotoAdapter photoAdapter;
  private PhotosViewModel photosViewModel;
  private Activity activity;
  private View view;
  private TextView textView;

  /**
   * Finds or create a PhotoFragment using FragmentManager. Used to retain state on rotation
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
    view = inflater.inflate(R.layout.fragment_photos, container, false);

    // Initialise view if has access, else displays a text notice
    textView = view.findViewById(R.id.text_notifications);

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
        initializeRecyclerView();
      }
    });

    if (!permissionHelper.hasStoragePermission()) {
      textView.setText("Please Enable Storage Access to use this feature");
    }
  }

  /**
   * Build an AlertDialog to display the rationale
   *
   * @param isSettingsDialog True to show "Settings" (brings user to application details setting,
   * only when the permission is set as "Never ask again") instead of "Continue"
   */
  private void buildDialog(boolean isSettingsDialog) {
    String message = "To access your photos, allow Photour access to your device's storage. "
        + (isSettingsDialog ? "Tap Settings > Permissions, and turn Storage ON." : "");

    AlertDialogHelper alertDialogHelper = new AlertDialogHelper(activity, message);
    alertDialogHelper.initAlertDialog(STORAGE_PERMISSION_CODE);
    alertDialogHelper.initBuilder();

    if (isSettingsDialog) {
      alertDialogHelper.buildSettingsDialog();
    } else {
      alertDialogHelper.buildContinueDialog(permissionHelper, this::initializeRecyclerView);
    }
  }

  /**
   * Initialize recycler view for photos
   */
  private void initializeRecyclerView() {

    // Initialize lists for SectionedGridRecyclerViewAdapter
    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<Uri> uris = new ArrayList<>();

    textView.setText("");

    // Sets up recycler view and view model
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    RecyclerView mRecyclerView = view.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setHasFixedSize(true);
    int IMAGE_WIDTH = 100;
    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
        PhotosViewModel.calculateNoOfColumns(Objects.requireNonNull(getContext()), IMAGE_WIDTH)));

    // Sets up adapters, photoAdapter is in charge of images, mSectionedAdapter for titles and grid
    photoAdapter = new PhotoAdapter(getContext());
    SectionedGridRecyclerViewAdapter.Section[] dummy =
        new SectionedGridRecyclerViewAdapter.Section[sections.size()];
    SectionedGridRecyclerViewAdapter mSectionedAdapter = new
        SectionedGridRecyclerViewAdapter(Objects.requireNonNull(getActivity()),
        R.layout.fragment_photos_sort, R.id.sorted_title_view, mRecyclerView, photoAdapter);

    // set observer to image list, on calls adapters to reset
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

      // Parses values into adapters and update view
      photoAdapter.setItems(uris);
      photoAdapter.notifyDataSetChanged();
      mSectionedAdapter.setSections(sections.toArray(dummy));
      mSectionedAdapter.notifyDataSetChanged();
      mRecyclerView.setAdapter(mSectionedAdapter);
    });
  }

  /**
   * Initialize the contents of the Fragment host's standard options menu.
   *
   * @param menu The options menu in which the items are placed
   *
   * @see #setHasOptionsMenu
   * @see #onPrepareOptionsMenu
   * @see #onOptionsItemSelected
   */
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.findItem(R.id.menu_filter).setVisible(true);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == R.id.by_date) {
      photosViewModel.switchSortMode(PhotosViewModel.QUERY_BY_DATE);
    } else if (itemId == R.id.by_path) {
      photosViewModel.switchSortMode(PhotosViewModel.QUERY_BY_TRIPS);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
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

    permissionHelper.onRequestPermissionsResult(grantResults, this::initializeRecyclerView);
  }
}
