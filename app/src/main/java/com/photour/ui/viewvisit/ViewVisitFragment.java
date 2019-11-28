package com.photour.ui.viewvisit;

import android.Manifest;
import android.app.Activity;
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
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.databinding.FragmentViewVisitBinding;
import com.photour.helper.PermissionHelper;
import com.photour.model.ImageElement;
import com.photour.model.Visit;

import java.util.List;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

/**
 * Fragment for ViewVisit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ViewVisitFragment extends Fragment implements OnMapReadyCallback {

  private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private FragmentViewVisitBinding binding;
  private Activity activity;

  private GoogleMap googleMap;
  private Visit visit;
  private ViewVisitViewModel viewVisitViewModel;
  private ViewPager2 mViewPager;
  private ViewVisitAdapter viewVisitAdapter;

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
   * and loads images.
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
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    viewVisitViewModel = new ViewModelProvider(this).get(ViewVisitViewModel.class);

    binding = FragmentViewVisitBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setVisitItem(viewVisitViewModel);

    if (getArguments() != null) {
      visit = ViewVisitFragmentArgs.fromBundle(getArguments()).getVisit();
      viewVisitViewModel.visit = visit;
      viewVisitViewModel.loadImages();
    }

    ((MainActivity) activity).setToolbarTitle("");

    return binding.getRoot();
  }

  /**
   * Called when Fragment View has been inflated. Map is initialised here.
   *
   * @param view View for the fragment's UI
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

    if (visit != null) { initializeViewPager();}
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

    mViewPager = binding.imageScroll;
    viewVisitAdapter = new ViewVisitAdapter();
    mViewPager.setAdapter(viewVisitAdapter);

    viewVisitViewModel.images.observe(getViewLifecycleOwner(), this::resetViewPager);
  }

  /**
   * Helper function to reset viewPager when dataset changes
   * Adds listener to ViewPager to update details onPageChange
   *
   * @param photos List of ImageElement
   */
  private void resetViewPager(List<ImageElement> photos) {
    if (photos != null && photos.size() > 0) {
      LatLng point = viewVisitViewModel.setDetails(mViewPager.getCurrentItem());
      setMarker(point);
      viewVisitAdapter.setItems(photos);
      viewVisitAdapter.notifyDataSetChanged();
      viewVisitViewModel.setPlaceholderText(true);
      mViewPager.registerOnPageChangeCallback(callback);
    } else {
      viewVisitViewModel.setPlaceholderText(false);
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
  }

  /**
   * Helper function to set the marker for ImageElement when the ViewPager is scrolled
   *
   * @param point LatLng object parsed from ImageElement
   */
  public void setMarker(LatLng point) {
    this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
    this.googleMap.addMarker(new MarkerOptions().position(point));
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

  private ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
      super.onPageSelected(position);
      LatLng point = viewVisitViewModel.setDetails(position);
      setMarker(point);
    }
  };
}
