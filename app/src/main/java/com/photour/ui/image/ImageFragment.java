package com.photour.ui.image;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.databinding.FragmentImageBinding;
import com.photour.helper.PermissionHelper;
import com.photour.model.ImageElement;

import static com.photour.helper.PermissionHelper.STORAGE_PERMISSION_CODE;

/**
 * Fragment to create when an has been clicked
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ImageFragment extends Fragment implements OnMapReadyCallback {

  private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
  private PermissionHelper permissionHelper;

  private FragmentImageBinding binding;
  private Activity activity;

  private GoogleMap googleMap;
  private ImageElement image;

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
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentImageBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);

    if (getArguments() != null) {
      image = ImageFragmentArgs.fromBundle(getArguments()).getImage();
      binding.setImage(image);
    }

    ((MainActivity) activity).setToolbarTitle("");

    return binding.getRoot();
  }

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
  }

  @Override
  public void onResume() {
    super.onResume();

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
    this.googleMap = googleMap;
    this.googleMap.getUiSettings().setMapToolbarEnabled(false);

    LatLng point = new LatLng(image.latitude(), image.longitude());
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
}
