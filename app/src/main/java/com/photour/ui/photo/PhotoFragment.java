package com.photour.ui.photo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.photour.R;
import com.photour.database.VisitRepository;
import com.photour.databinding.FragmentPhotoBinding;
import com.photour.helper.PermissionHelper;
import com.photour.helper.PreferenceHelper;
import com.photour.model.Photo;

/**
 * Fragment to create when an photo has been clicked
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotoFragment extends Fragment implements OnMapReadyCallback {

  private PermissionHelper permissionHelper;

  private FragmentPhotoBinding binding;
  private Activity activity;

  private Photo photo;

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
    permissionHelper = PermissionHelper.getStoragePermissionHelper(activity, this);
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
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentPhotoBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setFragment(this);

    if (getArguments() != null) {
      VisitRepository visitRepository = new VisitRepository(activity.getApplication());
      photo = PhotoFragmentArgs.fromBundle(getArguments()).getPhoto();
      binding.setPhoto(photo);
      binding.setUnit(PreferenceHelper.tempUnit(getContext()));
      binding.setVisitTitle(visitRepository.getVisitTitle(photo.visitId()));
    }

    BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet);

    binding.imageRaw.bottomSheet.setBottomSheet(binding.detailTag, bottomSheetBehavior);

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
    // Inflate MapFragment lite mode. Lite mode only work when using ViewStub to inflate it
    ViewStub viewStub = binding.viewstubMap.getViewStub();
    if (viewStub != null) {
      viewStub.inflate();
    }

    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map_lite_fragment);

    if (supportMapFragment != null) {
      // Disable click events in lite mode, prevent opening Google Maps
      View mapFragmentView = supportMapFragment.getView();
      if (mapFragmentView != null) {
        supportMapFragment.getView().setClickable(false);
      }

      supportMapFragment.getMapAsync(this);
    }
  }

  /**
   * Called when fragment is initialised or resumed. Checks if has storage permission else exit to
   * previous fragment
   */
  @Override
  public void onResume() {
    super.onResume();

    // User might have revoked the permission
    if (!permissionHelper.hasStoragePermission()) {
      Navigation.findNavController(binding.getRoot()).navigateUp();
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
    googleMap.getUiSettings().setMapToolbarEnabled(false);
    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(photo.latLng(), 15));
    googleMap.addMarker(new MarkerOptions().position(photo.latLng()));
  }

  /**
   * Initialize the contents of the Fragment host's standard options menu.
   *
   * @param menu The options menu in which you place your items.
   * @see #setHasOptionsMenu
   * @see #onPrepareOptionsMenu
   * @see #onOptionsItemSelected
   */
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    menu.clear();
    activity.getMenuInflater().inflate(R.menu.menu_photo, menu);
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
    if (item.getItemId() == R.id.photo_details) {
      toggleBottomSheet();
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Toggle the bottom sheet between expanded and collapse state
   */
  public void toggleBottomSheet() {
    binding.imageRaw.bottomSheet.toggleBehaviorState();
  }
}
