package com.photour.ui.visit;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.BitmapDescriptor;
import com.google.android.libraries.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.maps.model.JointType;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.android.libraries.maps.model.Marker;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.databinding.FragmentVisitBinding;
import com.photour.helper.PermissionHelper;
import com.photour.model.Photo;
import com.photour.model.Visit;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for ViewVisit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitFragment extends Fragment implements OnMapReadyCallback {

  private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private FragmentVisitBinding binding;
  private Activity activity;

  private GoogleMap googleMap;
  private Visit visit;
  private VisitViewModel visitViewModel;
  private ViewPager2 mViewPager;
  private VisitAdapter visitAdapter;
  private List<Marker> markerList = new ArrayList<>();

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
   * Called to have the fragment instantiate its user interface view. Visit argument is parsed here
   * and loads photos.
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
  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);

    binding = FragmentVisitBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setFragment(this);
    binding.setVisitItem(visitViewModel);

    if (getArguments() != null) {
      visit = VisitFragmentArgs.fromBundle(getArguments()).getVisit();
      visitViewModel.visit = visit;
      visitViewModel.loadImages();
    }

    ((MainActivity) activity).setToolbarTitle(visitViewModel.visit.visitTitle());

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
    if (visit != null) {
      initializeViewPager();
    }
  }

  /**
   * Called when fragment is initialised or resumed. Checks if has storage permission else exit to
   * previous fragment
   */
  @Override
  public void onResume() {
    super.onResume();

    if (!permissionHelper.hasStoragePermission()) {
      Navigation.findNavController(binding.getRoot()).navigateUp();
    }
  }

  /**
   * Helper function to initialise ViewPager and observe image in ViewModel
   */
  private void initializeViewPager() {
    inflateMap();

    mViewPager = binding.imageScroll;
    visitAdapter = new VisitAdapter();
    mViewPager.setAdapter(visitAdapter);

    visitViewModel.photos.observe(getViewLifecycleOwner(), this::resetViewPager);
  }

  /**
   * Helper function to reset viewPager when dataset changes Adds listener to ViewPager to update
   * details onPageChange
   *
   * @param photos List of {@link Photo}
   */
  private void resetViewPager(List<Photo> photos) {
    if (photos == null || photos.isEmpty()) {
      visitViewModel.setPlaceholderText(false);
      visitViewModel.setDetails(-1);
    } else {
      initialiseMarker(photos);
      visitAdapter.setItems(photos);
      visitAdapter.notifyDataSetChanged();
      visitViewModel.setPlaceholderText(true);
      mViewPager.registerOnPageChangeCallback(callback);
    }
  }

  /**
   * Inflate the ViewStub, then initialise the Google Map
   */
  private void inflateMap() {
    // Inflate MapFragment lite mode. Lite mode only work when using ViewStub to inflate it
    ViewStub viewStub = binding.viewstubMap.getViewStub();
    if (viewStub != null) {
      viewStub.inflate();
    }

    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map_lite_fragment);

    if (supportMapFragment != null) {
      // Disable click events in lite mode, prevent opening Google Maps
      View mapview = supportMapFragment.getView();
      if (mapview != null) {
        supportMapFragment.getView().setClickable(false);
      }

      supportMapFragment.getMapAsync(this);
    }
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
    this.googleMap.getUiSettings().setMapToolbarEnabled(false);

    initialisePolyLine();
  }

  /**
   * Function to initialise polyline to indicate visit path Starts with getting LatLngBounds for the
   * camera to be able to fit in the entire visit Finally add all LatLng into PolyLine Object to
   * draw it on the map
   */
  private void initialisePolyLine() {
    List<LatLng> polyLine = visit.latLngList();

    // Edge case: latLngList is null or empty
    if (polyLine == null || polyLine.isEmpty()) {
      return;
    }

    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (LatLng latLng : polyLine) {
      builder.include(latLng);
    }

    LatLngBounds bounds = builder.build();
    this.googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

    PolylineOptions options = new PolylineOptions()
        .width(5)
        .color(Color.rgb(190, 41, 236))
        .jointType(JointType.BEVEL)
        .addAll(polyLine);
    this.googleMap.addPolyline(options);
  }

  /**
   * Add in all the markers of the photo on the map The markers are then added into a list for
   * future reference
   *
   * @param photos The list of photos in the visit to be added as marker
   */

  private void initialiseMarker(List<Photo> photos) {
    for (Photo photo : photos) {
      Marker marker = this.googleMap.addMarker(new MarkerOptions().position(photo.latLng()));
      marker.setTag(photo.id());
      markerList.add(marker);
    }

    setMarker(1);
  }

  /**
   * Helper function to set the marker for Photo when the ViewPager is scrolled
   *
   * @param id id parsed from Photo
   */
  private void setMarker(int id) {
    for (Marker marker : markerList) {
      boolean photoEqualsMarkerTag = id == (int) marker.getTag();

      marker.setZIndex(photoEqualsMarkerTag ? 1f : 0);

      if (photoEqualsMarkerTag) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
      } else {
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.unfocused_marker);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 50, 50, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        marker.setIcon(smallMarkerIcon);
      }

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
   * Callback on page scroll on ViewPager Details for the image are parsed through ViewModel and
   * marker for the image is set on the map.
   */
  private ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
      super.onPageSelected(position);
      int id = visitViewModel.setDetails(position);
      setMarker(id);
    }
  };
}
