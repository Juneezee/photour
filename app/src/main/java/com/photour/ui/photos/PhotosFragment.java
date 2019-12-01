package com.photour.ui.photos;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest.permission;
import android.app.Activity;
import android.graphics.Bitmap;
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
import com.photour.R;
import com.photour.databinding.FragmentPhotosBinding;
import com.photour.helper.PermissionHelper;
import com.photour.model.Photo;
import com.photour.model.SectionElement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

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

  private SectionedGridRecyclerViewAdapter mSectionedAdapter;
  private RecyclerView mRecyclerView;
  private PhotoAdapter photoAdapter;

  private PhotosViewModel photosViewModel;
  private FragmentPhotosBinding binding;
  private Activity activity;

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

    binding = FragmentPhotosBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setViewModel(photosViewModel);

    return binding.getRoot();
  }


  /**
   * Called when the fragment is created or resumed
   */
  @Override
  public void onResume() {
    super.onResume();
    photosViewModel.setPlaceholderText(permissionHelper.hasStoragePermission());

    // Check if storage permission is granted or not
    permissionHelper.checkStoragePermission(this::initializeRecyclerView);
  }

  /**
   * Initialize recycler view for photos
   */
  private void initializeRecyclerView() {
    photosViewModel.loadPhotos();
    photosViewModel.setPlaceholderText(true);

    // Sets up recycler view and view model
    mRecyclerView = binding.gridRecyclerView;
    mRecyclerView.setHasFixedSize(true);
    int IMAGE_WIDTH = 100;
    mRecyclerView.setLayoutManager(new GridLayoutManager(activity,
        PhotosViewModel.calculateNoOfColumns(activity, IMAGE_WIDTH)));

    // Sets up adapters, photoAdapter is in charge of photos, mSectionedAdapter for titles and grid
    photoAdapter = new PhotoAdapter();

    mSectionedAdapter = new
        SectionedGridRecyclerViewAdapter(activity, R.layout.fragment_photos_sort,
        R.id.sorted_title_view, mRecyclerView, photoAdapter);

    // set observer to image list, on calls adapters to reset
    photosViewModel.photos.observe(getViewLifecycleOwner(), this::resetGrid);
  }

  /**
   * Function to reload recycler view. Splits Photos into section.
   *
   * @param photos List of Photos
   */

  private void resetGrid(List<Photo> photos) {

    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<Photo> photoList = new ArrayList<>();
    SectionedGridRecyclerViewAdapter.Section[] dummy =
        new SectionedGridRecyclerViewAdapter.Section[sections.size()];

    List<SectionElement> sectionElements = sectionImages(photos);

    // Prompts text if no photos, else load photos into lists
    if (photos == null || photos.size() == 0) {
      photosViewModel.setPlaceholderText(false);
    } else {
      int pos = 0;
      photosViewModel.setPlaceholderText(true);

      for (SectionElement sectionElement : sectionElements) {
        sections.add(new SectionedGridRecyclerViewAdapter.Section(pos, sectionElement.getTitle()));
        pos += sectionElement.getPhotos().size(); // add number of photos and title

        photoList.addAll(sectionElement.getPhotos());
      }
    }

    // Parses values into adapters and update view
    photoAdapter.setItems(photoList);
    photoAdapter.notifyDataSetChanged();
    mSectionedAdapter.setSections(sections.toArray(dummy));
    mSectionedAdapter.notifyDataSetChanged();
    mRecyclerView.setAdapter(mSectionedAdapter);
  }

  /**
   * Uses Mediastore query to get all photos from a given folder according to the sort
   * configuration.
   *
   * @return List lists of SectionElement, each representing a section in the gallery
   */
  private List<SectionElement> sectionImages(List<Photo> images) {
    List<SectionElement> sections = new ArrayList<>();
    Hashtable<String, Integer> titles = new Hashtable<>();

    if (images != null) {
      int i = photosViewModel.sortMode == R.id.by_date_asc ? images.size() - 1 : 0;
      int limit = photosViewModel.sortMode == R.id.by_date_asc ? -1 : images.size();

      // Iterates through query and append them into SectionElement
      while (i != limit) {
        Photo photo = images.get(i);
        String currentTitle;
        if (photosViewModel.sortMode == R.id.by_date_asc
            || photosViewModel.sortMode == R.id.by_date_desc) {
          Date date = photo.date();
          SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
          currentTitle = sdf.format(date);
        } else {
          currentTitle = ""; //TODO
        }
        if (!titles.containsKey(currentTitle)) {
          sections.add(new SectionElement(currentTitle));
          titles.put(currentTitle, sections.size() - 1);
        }

        sections.get(titles.get(currentTitle)).addPhoto(photo);
        if (photosViewModel.sortMode == R.id.by_date_asc) {
          i--;
        } else {
          i++;
        }
      }
    }
    return sections;
  }

  /**
   * Switch sorting mode and call resetGrid() to reload recyclerView
   *
   * @param type The type to sort the photos (by date or by path)
   */
  private void switchSortMode(int type) {
    if (photosViewModel.sortMode != type) {
      photosViewModel.sortMode = type;
      resetGrid(photosViewModel.photos.getValue());
    }
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
    menu.findItem(R.id.photos_filter).setVisible(true);
    menu.findItem(R.id.photos_map).setVisible(true);
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

    if (permissionHelper.hasStoragePermission()) {
      switch (itemId) {
        case R.id.by_date_desc:
        case R.id.by_date_asc:
        case R.id.by_visit:
          switchSortMode(itemId);
          break;
      }
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
