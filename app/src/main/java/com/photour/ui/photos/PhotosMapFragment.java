package com.photour.ui.photos;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.maps.android.clustering.ClusterManager;
import com.photour.R;
import com.photour.helper.PermissionHelper;
import com.photour.model.Photo;
import java.util.List;

/**
 * Fragment to create when the map icon has been clicked on {@link PhotosFragment}, show "location
 * album"
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosMapFragment extends Fragment implements OnMapReadyCallback {

  private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private PhotosViewModel photosViewModel;
  private Activity activity;
  private View view;

  private GoogleMap googleMap;
  private ClusterManager<ClusterMarker> clusterManager;

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
    activity = getActivity();
    setHasOptionsMenu(true);
    permissionHelper = new PermissionHelper(activity, this, PERMISSIONS_REQUIRED);
    permissionHelper.setRequestCode(STORAGE_PERMISSION_CODE);
  }

  /**
   * Called to have the fragment instantiate its user interface view.
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
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    view = inflater.inflate(R.layout.fragment_photos_map, container, false);
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

    if (savedInstanceState == null) {
      // Prevent mini-lag when the map icon is clicked for first time
      new Handler().post(this::initGoogleMap);
    } else {
      initGoogleMap();
    }
  }

  /**
   * Prepare the Fragment host's standard options menu to be displayed.
   *
   * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
   * @see #setHasOptionsMenu
   * @see #onCreateOptionsMenu
   */
  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    // Do not show any menu items
    menu.clear();
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

    permissionHelper.onRequestPermissionsResult(grantResults, this::initGoogleMap);
  }

  /**
   * Called when the map is ready to be used.
   *
   * @param googleMap A non-null instance of a GoogleMap associated with the MapFragment or MapView
   * that defines the callback.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    this.googleMap.getUiSettings().setZoomControlsEnabled(true);
    this.googleMap.getUiSettings().setMapToolbarEnabled(false);

    permissionHelper.checkStoragePermission(this::setUpCluster);
  }

  /**
   * Initialise Google Map
   */
  private void initGoogleMap() {
    view.findViewById(R.id.viewstub_map).setVisibility(View.VISIBLE);

    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map_fragment);

    if (supportMapFragment != null) {
      supportMapFragment.getMapAsync(this);
    }
  }

  /**
   * Set up <var>clusterManager</var>
   */
  private void setUpCluster() {
    // Initialize the manager with the context and the map.
    clusterManager = new ClusterManager<>(activity, googleMap);

    googleMap.setOnCameraIdleListener(clusterManager);
    googleMap.setOnMarkerClickListener(clusterManager);

    List<Photo> photos = photosViewModel.loadPhotosForClusterMarker();

    for (Photo photo : photos) {
      clusterManager.addItem(new ClusterMarker(photo.filePath(), photo.latLng()));
    }
  }

}
