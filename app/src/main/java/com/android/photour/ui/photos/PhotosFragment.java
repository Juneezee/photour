package com.android.photour.ui.photos;

import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest.permission;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.photour.ImageElement;
import com.android.photour.R;
import com.android.photour.databinding.FragmentPhotosBinding;
import com.android.photour.helper.AlertDialogHelper;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.helper.PermissionHelper.PermissionAskListener;
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

  private static final String[] PERMISSIONS_REQUIRED = {permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private PhotoAdapter photoAdapter;
  private PhotosViewModel photosViewModel;
  private Activity activity;
  private View view;

  /**
   * Finds or create a PhotoFragment using FragmentManager. Used to retain state on rotation
   *
   * @param fm FragmentManager
   * @return PhotoFragment
   */
  public static PhotosFragment findOrCreateRetainFragment(FragmentManager fm) {
    PhotosFragment fragment = (PhotosFragment) fm.findFragmentByTag(TAG);
    return fragment == null ? new PhotosFragment() : fragment;
  }

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
    setHasOptionsMenu(true);

    activity = getActivity();
    permissionHelper = new PermissionHelper(activity, this, PERMISSIONS_REQUIRED);
    permissionHelper.setRequestCode(STORAGE_PERMISSION_CODE);
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
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);

    FragmentPhotosBinding binding = FragmentPhotosBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setPlaceholder(photosViewModel);

    view = binding.getRoot();

    return view;
  }

  /**
   * Called when the Fragment is visible to the user.  This is generally tied to Activity.onStart()
   * of the containing Activity's lifecycle.
   */
  @Override
  public void onStart() {
    super.onStart();

    photosViewModel.setPlaceholderText(permissionHelper.hasStoragePermission());

    // Check if storage permission is granted or not
    permissionHelper.checkStoragePermission(this::initializeRecyclerView);
  }

  /**
   * Initialize recycler view for photos
   */
  private void initializeRecyclerView() {

    // Initialize lists for SectionedGridRecyclerViewAdapter
    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<Uri> uris = new ArrayList<>();

    photosViewModel.setPlaceholderText(true);

    // Sets up recycler view and view model
    photosViewModel.loadImages();
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
        photosViewModel.setPlaceholderText(false);
      } else {
        int pos = 0;
        photosViewModel.setPlaceholderText(true);
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
   * @see #setHasOptionsMenu
   * @see #onPrepareOptionsMenu
   * @see #onOptionsItemSelected
   */
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.findItem(R.id.menu_filter).setVisible(permissionHelper.hasStoragePermission());
  }

  /**
   * This hook is called whenever an item in options menu is selected.
   *
   * @param item The menu item that was selected.
   * @return boolean Return false to allow normal menu processing to proceed, true to consume it
   * here.
   * @see #onCreateOptionsMenu
   */
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == R.id.by_date) {
      photosViewModel.switchSortMode(PhotosViewModel.QUERY_BY_DATE);
    } else if (itemId == R.id.by_path) {
      photosViewModel.switchSortMode(PhotosViewModel.QUERY_BY_PATH);
    }

    return super.onOptionsItemSelected(item);
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
