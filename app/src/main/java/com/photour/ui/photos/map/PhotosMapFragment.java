package com.photour.ui.photos.map;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.android.libraries.maps.model.Marker;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener;
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.photour.R;
import com.photour.helper.PermissionHelper;
import com.photour.model.Photo;
import com.photour.task.ClusterTask;
import com.photour.ui.photos.PhotosFragment;
import com.photour.ui.photos.PhotosViewModel;
import com.photour.ui.photos.map.PhotosMapFragmentDirections.ActionViewPhotos;
import java.util.List;

/**
 * Fragment to create when the map icon has been clicked on {@link PhotosFragment}, show "location
 * album"
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosMapFragment extends Fragment implements OnMapReadyCallback,
    OnClusterClickListener<Photo>, OnClusterItemClickListener<Photo> {

  private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private PhotosViewModel photosViewModel;
  private Activity activity;
  private View view;

  private GoogleMap googleMap;
  private ClusterManager<Photo> clusterManager;

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
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
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
   * Called when the Fragment is no longer resumed.  This is generally tied to Activity.onPause of
   * the containing Activity's lifecycle.
   */
  @Override
  public void onPause() {
    super.onPause();
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

    permissionHelper.onRequestPermissionsResult(grantResults, this::setUpCluster);
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
    // Do not re-initialise map to prevent marker not being clustered after onImageClick()
    if (googleMap != null) {
      return;
    }

    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map_fragment);

    if (supportMapFragment != null) {
      supportMapFragment.getMapAsync(this);
    }
  }

  /**
   * Draws photos inside markers (using IconGenerator) When there are multiple photos in the
   * cluster, draw multiple photos (using MultiDrawable)
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class PhotoRenderer extends DefaultClusterRenderer<Photo> {

    private final IconGenerator iconGenerator = new IconGenerator(activity);
    private final ImageView imageView;

    PhotoRenderer() {
      super(activity, googleMap, clusterManager);
      int dimension = (int) getResources().getDimension(R.dimen.custom_photo_marker);

      int padding = (int) getResources().getDimension(R.dimen.custom_photo_padding);

      imageView = new ImageView(activity);
      imageView.setLayoutParams(new LayoutParams(dimension, dimension));
      imageView.setPadding(padding, padding, padding, padding);
      iconGenerator.setContentView(imageView);
    }

    /**
     * Called before the marker for a ClusterItem is added to the map.
     */
    @Override
    protected void onBeforeClusterItemRendered(Photo item, MarkerOptions markerOptions) {
      // Draw a placeholder layout first
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     *
     * @param item A {@link Photo} object
     * @param marker The {@link Marker} that has been created
     */
    @Override
    protected void onClusterItemRendered(Photo item, Marker marker) {
      // Create an async task to update the photo on the marker
      new ClusterTask(imageView, () ->
          marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
      ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.filePath());
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     */
    @Override
    protected boolean shouldRenderAsCluster(Cluster<Photo> cluster) {
      // Always render clusters.
      return cluster.getSize() > 1;
    }
  }

  /**
   * Called when cluster is clicked.
   *
   * @param cluster The cluster that is clicked
   * @return boolean {@code true} if click has been handled, {@code false} and the click will
   * dispatched to the next listener
   */
  @Override
  public boolean onClusterClick(Cluster<Photo> cluster) {
    if (cluster.getSize() <= 50) {
      ActionViewPhotos actionViewPhotos = PhotosMapFragmentDirections.actionViewPhotos();
      actionViewPhotos.setPhotos(cluster.getItems().toArray(new Photo[0]));
      Navigation.findNavController(view).navigate(actionViewPhotos);

      return true;
    }

    // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
    // inside of bounds, then animate to center of the bounds.
    // Create the builder to collect all essential cluster items for the bounds.
    LatLngBounds.Builder builder = LatLngBounds.builder();
    for (ClusterItem item : cluster.getItems()) {
      builder.include(item.getPosition());
    }

    // Animate camera to the bounds by calling builder.build()
    try {
      googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * Called when an individual ClusterItem is clicked. Open {@link com.photour.ui.photo.PhotoFragment}
   * to show the photo details
   *
   * @param photo The photo that is clicked
   * @return boolean {@code true} if click has been handled, {@code false} and the click will
   * dispatched to the next listener
   */
  @Override
  public boolean onClusterItemClick(Photo photo) {
    photo.onImageClick(view);
    return true;
  }

  /**
   * Set up <var>clusterManager</var>
   */
  private void setUpCluster() {
    DisplayMetrics metrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

    // Initialize the manager with the context and the map. Set algorithm to enable lazy load
    clusterManager = new ClusterManager<>(activity, googleMap);
    clusterManager.setRenderer(new PhotoRenderer());
    clusterManager.setAlgorithm(
        new NonHierarchicalViewBasedAlgorithm<>(metrics.widthPixels, metrics.heightPixels));

    // Register click listener
    googleMap.setOnCameraIdleListener(clusterManager);
    googleMap.setOnMarkerClickListener(clusterManager);
    clusterManager.setOnClusterClickListener(this);
    clusterManager.setOnClusterItemClickListener(this);

    // Add items
    addPhotosToCluster();
    clusterManager.cluster();
  }

  /**
   * Retrive photos from database, then add them into the cluster manager
   */
  private void addPhotosToCluster() {
    List<Photo> photos = photosViewModel.loadPhotosForClusterMarker();

    for (Photo photo : photos) {
      clusterManager.addItem(photo);
    }

//    Date today = new Date();
//
//    for (int i = 0; i < 10000; i++) {
//      // /storage/emulated/0/Pictures/Photour/IMG_20191129_175157_879.jpg
//      // /storage/emulated/0/Pictures/Screenshots/Screenshot_20191126_121114_com.szckhd.jwgly.azfanti.jpg
//      clusterManager.addItem(
//          Photo.create(0, 0, "/storage/emulated/0/Pictures/Photour/IMG_20191129_175157_879.jpg",
//              today, new LatLng(
//                  ThreadLocalRandom.current().nextDouble(53.0, 53.5 + 1),
//                  ThreadLocalRandom.current().nextDouble(-1.5, -1.4 + 1)), null)
//      );
//    }
  }

}
