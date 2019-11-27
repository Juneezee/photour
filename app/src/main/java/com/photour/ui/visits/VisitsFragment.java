package com.photour.ui.visits;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest.permission;
import android.app.Activity;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.R;
import com.photour.databinding.FragmentVisitsBinding;
import com.photour.helper.PermissionHelper;
import com.photour.model.Visit;
import com.photour.ui.photos.PhotosFragment;

import java.util.Collections;
import java.util.List;

public class VisitsFragment extends Fragment {

  private static final String TAG = "VisitsFragment";

  private static final String[] PERMISSIONS_REQUIRED = {permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private VisitsViewModel visitsViewModel;
  private Activity activity;
  private FragmentVisitsBinding binding;
  private RecyclerView mRecyclerView;
  private VisitAdapter visitAdapter;

//  /**
//   * Finds or create a ImageFragment using FragmentManager. Used to retain state on rotation
//   *
//   * @param fm FragmentManager
//   * @return ImageFragment
//   */
//  public static VisitsFragment findOrCreateRetainFragment(FragmentManager fm) {
//    VisitsFragment fragment = (VisitsFragment) fm.findFragmentByTag(TAG);
//    return fragment == null ? new VisitsFragment() : fragment;
//  }
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
    visitsViewModel = new ViewModelProvider(this).get(VisitsViewModel.class);

    binding = FragmentVisitsBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setPlaceholder(visitsViewModel);

    return binding.getRoot();
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

    visitsViewModel.setPlaceholderText(permissionHelper.hasStoragePermission());

    // Check if storage permission is granted or not
    permissionHelper.checkStoragePermission(this::initializeRecyclerView);
  }

  private void initializeRecyclerView() {
    visitsViewModel.setPlaceholderText(true);

    mRecyclerView = binding.gridRecyclerView;
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

    visitAdapter = new VisitAdapter();
    mRecyclerView.setAdapter(visitAdapter);

    visitsViewModel.trips.observe(getViewLifecycleOwner(), this::resetRecyler);
  }

  private void resetRecyler(List<Visit> visits) {

    // Parses values into adapters and update view
    visitAdapter.setItems(visits);
    visitAdapter.notifyDataSetChanged();

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
    menu.findItem(R.id.trip_filter).setVisible(true);
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
        case R.id.trip_desc:
        case R.id.trip_asc:
          switchSortMode(itemId);
          break;
      }
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Switch sorting mode and call resetGrid() to reload recyclerView
   *
   * @param type The type to sort the photos (by date or by path)
   */
  private void switchSortMode(int type) {
    if (visitsViewModel.sortMode != type) {
      visitsViewModel.sortMode = type;
      Collections.reverse(visitsViewModel.trips.getValue());
      resetRecyler(visitsViewModel.trips.getValue());
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

    permissionHelper.onRequestPermissionsResult(grantResults, () -> {
    });
  }
}
