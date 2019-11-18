package com.android.photour.ui.visit;

import static android.content.Context.MODE_PRIVATE;
import static com.android.photour.helper.LocationServicesHelper.checkDeviceLocation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.android.photour.sensor.Accelerometer;
import com.android.photour.sensor.Barometer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.maps.CameraUpdate;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import java.util.Objects;

/**
 * Fragment to create when new visit has started
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private VisitViewModel visitViewModel;
  private Activity activity;
  private View view;

  private static final int ZOOM_LEVEL = 17;

  // Keys for storing activity state.
  private static final String KEY_CHRONOMETER = "chronometer";

  // To check if this created the first time in current activity
  private boolean isFirstTime = true;

  // Sensors
  private Accelerometer accelerometer;
  private Barometer barometer;

  // The entry point to the Fused Location Provider.
  private FusedLocationProviderClient fusedLocationProviderClient;

  private GoogleMap googleMap;
  private SupportMapFragment supportMapFragment;

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

    activity = getActivity();
    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
    view = inflater.inflate(R.layout.fragment_start_visit, container, false);

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

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

    // Add click listener to stop button
    stopVisitListener();

    // Retrieve the new visit title from VisitFragment
    TextView tv = view.findViewById(R.id.new_visit_title);
    String newVisitTitle = StartVisitFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()))
        .getNewVisitTitle();
    tv.setSelected(true);
    tv.setText(newVisitTitle.isEmpty() ? "Untitled trip" : newVisitTitle);

    // Prevent mini-slutter when the start button is pressed the first time
    if (savedInstanceState == null) {
      new Handler().post(this::initGoogleMap);
    } else {
      initGoogleMap();
    }

    // Initialise chronometer
    initChronometer();
  }

  /**
   * Add a click listener to the Stop button, to replace current fragment with {@link
   * VisitFragment}
   */
  private void stopVisitListener() {
    final Button stopButton = view.findViewById(R.id.button_stop_visit);
    stopButton.setOnClickListener(
        v -> Navigation.findNavController(view).navigate(R.id.action_stop_visit));
  }

  /**
   * Initialise the chronometer
   */
  private void initChronometer() {
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

  /**
   * Initialise Google Map
   */
  private void initGoogleMap() {
    view.findViewById(R.id.viewstub_map).setVisibility(View.VISIBLE);
    supportMapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map_fragment);

    if (supportMapFragment != null) {
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
    this.googleMap.setMyLocationEnabled(true);
    this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);

    if (isFirstTime) {
      LatLng lastLocation = getLastLocationFromPrefs();

      if (lastLocation != null) {
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, ZOOM_LEVEL));
      }

      zoomToCurrentLocation(false);
    }

    setLocationButton();
  }

  /**
   * Set the position of My Location button to bottom right, above Floating Action Camera Button.
   * Add permission and location services check to the click listener
   */
  private void setLocationButton() {
    View myLocation = view.findViewById(R.id.fab_mylocation);

    // Permission and location services check
    myLocation.setOnClickListener(
        v -> checkDeviceLocation(activity, this, () -> zoomToCurrentLocation(true)));
  }

  /**
   * Zoom the map to the current location of the device
   *
   * @param animate True for moving the camera with animation
   */
  private void zoomToCurrentLocation(boolean animate) {
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, location -> {
      if (location == null) {
        // The location is likely to be null when location services is faulty, request again
        checkDeviceLocation(activity, this, () -> zoomToCurrentLocation(true));
      } else {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        storeLastLocationInPrefs((float) latitude, (float) longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory
            .newLatLngZoom(new LatLng(latitude, longitude), ZOOM_LEVEL);

        if (animate) {
          googleMap.animateCamera(cameraUpdate);
        } else {
          googleMap.moveCamera(cameraUpdate);
        }
      }
    }).addOnFailureListener(e ->
        checkDeviceLocation(activity, this, () -> zoomToCurrentLocation(true)));
  }

  /**
   * Get the latitude and longitude of last known location from MainActivity.xml
   *
   * @return LatLng A LatLng instance of the last known location
   */
  private LatLng getLastLocationFromPrefs() {
    SharedPreferences sharedPreference = activity.getPreferences(MODE_PRIVATE);

    double latitude = sharedPreference.getFloat("latitude", 0);
    double longitude = sharedPreference.getFloat("longitude", 0);

    return latitude == 0 && longitude == 0 ? null : new LatLng(latitude, longitude);
  }

  /**
   * Store the latitude and longitude of last known location into MainActivity.xml
   *
   * @param latitude The latitude of last known location
   * @param longitude The longitude of last know location
   */
  private void storeLastLocationInPrefs(float latitude, float longitude) {
    SharedPreferences sharedPreference = activity.getPreferences(MODE_PRIVATE);
    sharedPreference.edit().putFloat("latitude", latitude).apply();
    sharedPreference.edit().putFloat("longitude", longitude).apply();
  }

  /**
   * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
   * in a new instance of its process is restarted.  If a new instance of the fragment later needs
   * to be created, the data placed in the Bundle here will be available in the Bundle given to
   * {@link #onCreate(Bundle)}, {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and {@link
   * #onActivityCreated(Bundle)}.
   *
   * @param outState Bundle in which to place your saved state.
   */
  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    outState.putLong(KEY_CHRONOMETER, visitViewModel.getElapsedTime());
    super.onSaveInstanceState(outState);
  }

  /**
   * Called when all saved state has been restored into the view hierarchy of the fragment.  This
   * can be used to do initialization based on saved state that you are letting the view hierarchy
   * track itself, such as whether check box widgets are currently checked.  This is called after
   * {@link #onActivityCreated(Bundle)} and before {@link #onStart()}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   * is the state.
   */
  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);

    if (savedInstanceState != null) {
      isFirstTime = false;

      // Restore the chronometer time
      visitViewModel.setElapsedTime(savedInstanceState.getLong(KEY_CHRONOMETER));
      initChronometer();
    }
  }

  /**
   * Called when the fragment is visible to the user and actively running.
   */
  @Override
  public void onResume() {
    super.onResume();
    ((MainActivity) activity).setToolbarVisibility(false);
  }

  /**
   * Called when the Fragment is no longer started.
   */
  @Override
  public void onStop() {
    super.onStop();
    ((MainActivity) activity).setToolbarVisibility(true);
  }
}
