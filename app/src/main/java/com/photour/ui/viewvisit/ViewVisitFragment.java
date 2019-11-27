package com.photour.ui.viewvisit;

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

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.databinding.FragmentImageBinding;
import com.photour.model.ImageElement;
import com.photour.model.Visit;
import com.photour.ui.visits.ViewVisitFragmentArgs;

import java.util.List;

public class ViewVisitFragment extends Fragment implements OnMapReadyCallback {

  private FragmentImageBinding binding;
  private Activity activity;

  private GoogleMap googleMap;
  private Visit visit;
  private List<ImageElement> visitPhotos;
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
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    binding = FragmentImageBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);

    if (getArguments() != null) {
      visit = ViewVisitFragmentArgs.fromBundle(getArguments()).getVisit();

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
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    this.googleMap.getUiSettings().setMapToolbarEnabled(false);

    // TODO
//    LatLng point = new LatLng(image.latitude(), image.longitude());
//    this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
//    this.googleMap.addMarker(new MarkerOptions().position(point));
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
