package com.android.photour.ui.visit;

import android.Manifest.permission;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.MapFragment;
import com.google.android.libraries.maps.MapView;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import java.util.Objects;

/**
 * Fragment to create when new visit has started
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private VisitViewModel visitViewModel;

  private static final int PERMISSION_REQUEST_CODE = 9001;
  private static final int PLAY_SERVICES_ERROR_CODE = 9002;
  private static final String TAG = "MapDebug";
  private boolean locationPermissionGranted;
  private GoogleMap googleMap;
  private MapView mapView;

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
      ViewGroup container,
      Bundle savedInstanceState) {

    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);

    return inflater.inflate(R.layout.fragment_visit_map, container, false);
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

    // Add click listener to stop button
    stopVisitListener(view);

    // Initialise chronometer
    initChronometer(view);

//    initGoogleMap();
  }

  /**
   * Add a click listener to the Stop button, to replace current fragment with {@link
   * VisitFragment}
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void stopVisitListener(View view) {
    final Button stopButton = view.findViewById(R.id.button_stop_visit);
    Fragment visitFragment = new VisitFragment();

    stopButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
        .replace(R.id.nav_host_fragment, visitFragment)
        .addToBackStack(null)
        .commit());
  }

  /**
   * Initialise
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void initChronometer(View view) {
    Chronometer chronometer = view.findViewById(R.id.chronometer);

    if (visitViewModel.getElapsedTime() == null) {
      // If the elapsed time is not defined, it's a new ViewModel so set it.
      long startTime = SystemClock.elapsedRealtime();
      visitViewModel.setElapsedTime(startTime);
      chronometer.setBase(startTime);
      System.out.println("Not Retained");
    } else {
      // Otherwise the ViewModel has been retained, set the chronometer's base to the original
      // starting time.
      System.out.println("Retained");
      chronometer.setBase(visitViewModel.getElapsedTime());
    }

    chronometer.start();
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
  }

  private void initGoogleMap() {
    if (isServicesOk()) {
      if (checkLocationPermission()) {
        mapView.getMapAsync(this);
      } else {
        requestLocationPermission();
      }
    }
  }

  private boolean checkLocationPermission() {
    return ContextCompat.checkSelfPermission(
        (Objects.requireNonNull(getActivity())), permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED;
  }

  private boolean isServicesOk() {
    GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

    int result = googleApi.isGooglePlayServicesAvailable(getActivity());

    if (result == ConnectionResult.SUCCESS) {
      return true;
    } else if (googleApi.isUserResolvableError(result)) {
      Dialog dialog = googleApi
          .getErrorDialog(getActivity(), result, PLAY_SERVICES_ERROR_CODE, task ->
              Toast.makeText(getActivity(), "Dialog is cancelled by User", Toast.LENGTH_SHORT)
                  .show());
      dialog.show();
    } else {
      Toast.makeText(getActivity(), "Play services are required by this application",
          Toast.LENGTH_SHORT).show();
    }
    return false;
  }

  private void requestLocationPermission() {
    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
        permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == PERMISSION_REQUEST_CODE
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      locationPermissionGranted = true;
      Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getActivity(), "Permission not granted", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
//    SupportMapFragment smf = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment));
    ((MainActivity) Objects.requireNonNull(getActivity())).setToolbarVisibility(false);
  }

  @Override
  public void onStop() {
    super.onStop();
    ((MainActivity) Objects.requireNonNull(getActivity())).setToolbarVisibility(true);
  }
}
