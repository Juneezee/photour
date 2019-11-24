package com.android.photour.ui.visit;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Chronometer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.android.photour.databinding.FragmentStartVisitBinding;
import com.android.photour.helper.LocationServicesHelper;
import com.android.photour.sensor.Accelerometer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.maps.CameraUpdate;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.JointType;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.PolylineOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.EasyImage.ImageSource;

/**
 * Fragment to create when new visit has started
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private FragmentStartVisitBinding binding;
  private VisitViewModel visitViewModel;
  private Activity activity;

  private GoogleMap googleMap;
  private FusedLocationProviderClient fusedLocationProviderClient;

  // Sensors
  private Accelerometer accelerometer;

  // To check if this created the first time in current activity
  private boolean isFirstTime;

  // Keys for storing activity state.
  private static final String KEY_CHRONOMETER = "chronometer";
  private static final String KEY_POLYLINE = "polyline";
  private static final String KEY_MARKER = "marker";

  // Constants for Google Map setting and location request
  private static final int ZOOM_LEVEL = 17;
  private static final int UPDATE_INTERVAL = 20000;
  private static final int FASTEST_INTERVAL = 1000;
  private static final float MIN_DISPLACEMENT = 5; // 5 metres minimum movements

  private ArrayList<LatLng> latLngList = new ArrayList<>();
  private ArrayList<LatLng> markerList = new ArrayList<>();

  private PendingIntent pendingIntent;

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
    isFirstTime = savedInstanceState == null;
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
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
    accelerometer = new Accelerometer(activity);

    binding = FragmentStartVisitBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setFragment(this);
    binding.setViewModel(visitViewModel);
    binding.setTemperature(accelerometer.getAmbientSensor());
    binding.setPressure(accelerometer.getBarometer());

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

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

    initStartNewVisitTitle();

    // Prevent mini-lag when the start button is pressed the first time
    if (savedInstanceState == null) {
      new Handler().post(this::initGoogleMap);
    } else {
      initGoogleMap();
    }

    initChronometer();
    initEasyImage();
  }

  /**
   * Initialise the start new visit title
   */
  private void initStartNewVisitTitle() {
    // For horizontal scrolling effect, put in FrameLayout so chronometer won't reset the scroll
    binding.newVisitTitle.setSelected(true);

    // Receive the new visit title from VisitFragment
    if (getArguments() != null) {
      String newVisitTitle = StartVisitFragmentArgs.fromBundle(getArguments()).getNewVisitTitle();
      visitViewModel.setNewVisitTitle(newVisitTitle);
    }
  }

  /**
   * Initialise the chronometer
   */
  private void initChronometer() {
    Chronometer chronometer = binding.chronometer;

    if (visitViewModel.getElapsedTime() == null) {
      // If the elapsed time is not defined, it's a new ViewModel so set it.
      long startTime = SystemClock.elapsedRealtime();
      visitViewModel.setElapsedTime(startTime);
      chronometer.setBase(startTime);
    } else {
      // Otherwise the ViewModel has been retained, set the chronometer's base to the original
      // starting time.
      chronometer.setBase(visitViewModel.getElapsedTime());
    }

    chronometer.start();
  }

  /**
   * Initialise Google Map
   */
  private void initGoogleMap() {
    // Inflate SupportMapFragment
    ViewStub viewStub = binding.viewstubMap.getViewStub();
    if (viewStub != null) {
      viewStub.inflate();
    }

    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
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

    // Re-draw the polylines if fragment is re-created
    drawPolyline();

    startLocationUpdates();
  }

  private LocationCallback locationCallback = new LocationCallback() {
    @Override
    public void onLocationResult(LocationResult locationResult) {
      super.onLocationResult(locationResult);

      Location location = locationResult.getLastLocation();
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();

      LatLng latLng = new LatLng(latitude, longitude);
      latLngList.add(latLng);

      drawPolyline();

      // First location update should not animate to prevent fast zoom on initialisation
      if (isFirstTime) {
        isFirstTime = false;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
      } else {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
      }
    }
  };

  /**
   * Draw polyline on the map to represent the visit route
   */
  private void drawPolyline() {
    PolylineOptions polylineOptions = new PolylineOptions()
        .width(5)
        .color(Color.rgb(190, 41, 236))
        .jointType(JointType.BEVEL)
        .addAll(latLngList);
    googleMap.addPolyline(polylineOptions);
  }

  private void startLocationUpdates() {
//    Intent intent = new Intent(activity, LocationIntentService.class);
//    intent.setAction(LocationIntentService.ACTION_PROCESS_UPDATES);
//
//    if (VERSION.SDK_INT >= VERSION_CODES.O) {
//      pendingIntent = PendingIntent
//          .getForegroundService(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    } else {
//      pendingIntent = PendingIntent
//          .getService(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }

    LocationRequest locationRequest = new LocationRequest()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(UPDATE_INTERVAL)
        .setFastestInterval(FASTEST_INTERVAL)
        .setMaxWaitTime(UPDATE_INTERVAL)
        .setSmallestDisplacement(MIN_DISPLACEMENT);

    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

//    fusedLocationProviderClient.requestLocationUpdates(locationRequest, pendingIntent);
  }

  private void stopLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(pendingIntent);
  }

  /**
   * Zoom the map to the current location of the device
   */
  private void zoomToCurrentLocation() {
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, location -> {
      if (location == null) {
        // The location is likely to be null when location services is faulty, request again
        onMyLocationClick();
      } else {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL);

        googleMap.animateCamera(cameraUpdate);
      }
    }).addOnFailureListener(e -> onMyLocationClick());
  }

  /**
   * Set the position of My Location button to bottom right, above Floating Action Camera Button.
   * Add permission and location services check to the click listener
   */
  public void onMyLocationClick() {
    // Permission and location services check
    LocationServicesHelper
        .checkDeviceLocation(activity, this, this::zoomToCurrentLocation);
  }

  /**
   * Add a click listener to the Stop button, to replace current fragment with {@link
   * VisitFragment}
   */
  public void onStopClick() {
//    stopLocationUpdates();
    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_stop_visit);
  }

  /**
   * EasyImage initialisation
   */
  private void initEasyImage() {
    EasyImage.configuration(activity)
        .setImagesFolderName("Photour")
        .setCopyTakenPhotosToPublicGalleryAppFolder(true)
        .setCopyPickedImagesToPublicGalleryAppFolder(false)
        .setAllowMultiplePickInGallery(true);
  }

  /**
   * Open camera to take image
   */
  public void onCameraClick() {
    EasyImage.openCameraForImage(this, 0);
  }

  /**
   * Open gallery to choose image
   */
  public void onGalleryClick() {
    EasyImage.openGallery(this, 0);
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "LocationIntent";
      String description = "LocationIntentDesc";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel("photour", name, importance);
      channel.setDescription(description);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = activity
          .getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
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
    outState.putParcelableArrayList(KEY_POLYLINE, latLngList);
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
      // Restore polyline
      latLngList = savedInstanceState.getParcelableArrayList(KEY_POLYLINE);

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
    accelerometer.startAccelerometerRecording();
    ((MainActivity) activity).setToolbarVisibility(false);
  }

  /**
   * Called when the Fragment is no longer resumed.  This is generally tied to Activity.onPause() of
   * the containing Activity's lifecycle.
   */
  @Override
  public void onPause() {
    super.onPause();
    accelerometer.stopAccelerometer();
  }

  /**
   * Called when the Fragment is no longer started.
   */
  @Override
  public void onStop() {
    super.onStop();
    ((MainActivity) activity).setToolbarVisibility(true);
  }

  /**
   * Receive the result from a previous call to {@link #startActivityForResult(Intent, int)}.
   *
   * @param requestCode The integer request code originally supplied to startActivityForResult(),
   * allowing you to identify who this result came from.
   * @param resultCode The integer result code returned by the child activity through its
   * setResult().
   * @param data An Intent, which can return result data to the caller (various data can be attached
   * to Intent "extras").
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    EasyImage.handleActivityResult(requestCode, resultCode, data, activity, new DefaultCallback() {
      @Override
      public void onImagesPicked(@NonNull List<File> imageFiles, ImageSource source, int type) {
        // CODE TODO when upload from gallery
      }
    });
  }
}
