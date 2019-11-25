package com.android.photour.ui.photos;

import static com.android.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

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
import com.android.photour.R;
import com.android.photour.helper.PermissionHelper;
import com.android.photour.model.ImageElement;
import com.android.photour.model.SectionElement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for Photos page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosFragment extends Fragment {

  private static final String TAG = "PhotosFragment";
  private static final int QUERY_BY_DATE = 0;
  private static final int QUERY_BY_PATH = 1;
  private int sortMode;

  public static LruCache<String, Bitmap> mRetainedCache;

  private static final String[] PERMISSIONS_REQUIRED = {permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private PhotoAdapter photoAdapter;
  private SectionedGridRecyclerViewAdapter mSectionedAdapter;
  private PhotosViewModel photosViewModel;
  private RecyclerView mRecyclerView;
  private Activity activity;
  public List<ImageElement> elementList;

//  public static List<ImageElement> getElementList() {
//    return elementList;
//  }

  /**
   * Finds or create a ImageFragment using FragmentManager. Used to retain state on rotation
   *
   * @param fm FragmentManager
   * @return ImageFragment
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
    view = inflater.inflate(R.layout.fragment_photos, container, false);

    // Initialise view if has access, else displays a text notice
    textView = view.findViewById(R.id.text_placeholder);

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

    if (!permissionHelper.hasStoragePermission()) {
      textView.setText("Please Enable Storage Access to use this feature");
    }

    // Check if storage permission is granted or not
    permissionHelper.checkStoragePermission(this::initializeRecyclerView);
  }

  /**
   * Initialize recycler view for photos
   */
  private void initializeRecyclerView() {
    sortMode = QUERY_BY_DATE;
    // Initialize lists for SectionedGridRecyclerViewAdapter

    photosViewModel.setPlaceholderText(true);

    // Sets up recycler view and view model
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    RecyclerView mRecyclerView = view.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setHasFixedSize(true);
    int IMAGE_WIDTH = 100;
    mRecyclerView.setLayoutManager(new GridLayoutManager(activity,
        PhotosViewModel.calculateNoOfColumns(activity, IMAGE_WIDTH)));

    // Sets up adapters, photoAdapter is in charge of images, mSectionedAdapter for titles and grid
    photoAdapter = new PhotoAdapter(activity);

    mSectionedAdapter = new
        SectionedGridRecyclerViewAdapter(activity, R.layout.fragment_photos_sort,
        R.id.sorted_title_view, mRecyclerView, photoAdapter);

    // set observer to image list, on calls adapters to reset
    photosViewModel.images.observe(getViewLifecycleOwner(), this::resetGrid);
  }

  /**
   * Fucntion to reload recycler view. Splits ImageElements into section.
   *
   * @param imageElements List of ImageElements
   */

  private void resetGrid(List<ImageElement> imageElements) {

    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    elementList = new ArrayList<>();
    SectionedGridRecyclerViewAdapter.Section[] dummy =
        new SectionedGridRecyclerViewAdapter.Section[sections.size()];

    List<SectionElement> sectionElements = sectionImages(imageElements);
    //Prompts text if no images, else load images into lists
    if (imageElements == null || imageElements.size() == 0) {
      photosViewModel.setPlaceholderText(false);
    } else {
      int pos = 0;
      photosViewModel.setPlaceholderText(true);

      for (SectionElement sectionElement : sectionElements) {
        sections.add(new SectionedGridRecyclerViewAdapter.Section(pos, sectionElement.getTitle()));
        pos += sectionElement.getImageElements().size(); // add number of photos and title

        elementList.addAll(sectionElement.getImageElements());
      }
    }

    // Parses values into adapters and update view
    photoAdapter.setItems(elementList);
    photoAdapter.notifyDataSetChanged();
    mSectionedAdapter.setSections(sections.toArray(dummy));
    mSectionedAdapter.notifyDataSetChanged();
    mRecyclerView.setAdapter(mSectionedAdapter);
  }

  /**
   * Uses Mediastore query to get all images from a given folder according to the sort
   * configuration.
   *
   * @return List lists of SectionElement, each representing a section in the gallery
   */
  private List<SectionElement> sectionImages(List<ImageElement> images) {
    List<SectionElement> sections = new ArrayList<>();

    if (images != null) {
      int i = photosViewModel.sortMode == R.id.by_date_asc ? images.size() - 1 : 0;
      int limit = photosViewModel.sortMode == R.id.by_date_asc ? -1 : images.size();
      String previousTitle = "";
      SectionElement sectionElement = null;

      //Iterates through query and append them into SectionElement
      while (i != limit) {
        ImageElement imageElement = images.get(i);
        String currentTitle;
        if (photosViewModel.sortMode == R.id.by_date_asc
            || photosViewModel.sortMode == R.id.by_date_desc) {
          Date date = imageElement.getDate();
          SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
          currentTitle = sdf.format(date);
        } else {
          currentTitle = imageElement.getVisitTitle();
        }
        if (!previousTitle.equals(currentTitle)) {
          if (sectionElement != null) {
            sections.add(sectionElement);
          }
          sectionElement = new SectionElement(currentTitle);
          previousTitle = currentTitle;
        }

        sectionElement.addImageElement(imageElement);
        i++;
      }
      if (sectionElement != null) {
        sections.add(sectionElement);
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
    if (sortMode != type) {
      sortMode = type;
      resetGrid(photosViewModel.images.getValue());
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
    menu.findItem(R.id.menu_filter).setVisible(true);
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
    if (permissionHelper.hasStoragePermission()) {
      int itemId = item.getItemId();

      switch (itemId) {
        case R.id.by_date_desc:
        case R.id.by_date_asc:
        case R.id.by_path:
          photosViewModel.switchSortMode(itemId);
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
