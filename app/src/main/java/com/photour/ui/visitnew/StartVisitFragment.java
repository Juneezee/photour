package com.photour.ui.visitnew;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Chronometer;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.esafirm.imagepicker.features.ImagePicker;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.databinding.FragmentStartVisitBinding;
import com.photour.helper.AlertDialogHelper;
import com.photour.helper.LocationHelper;
import com.photour.helper.PermissionHelper;
import com.photour.helper.PreferenceHelper;
import com.photour.model.Photo;
import com.photour.sensor.Accelerometer;
import com.photour.sensor.AmbientSensor;
import com.photour.sensor.Barometer;
import com.photour.service.StartVisitService;
import java.util.Date;

/**
 * Fragment to create when new visit has started
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private static final String TAG = StartVisitFragment.class.getSimpleName();

  // Keys for storing activity state.
  private static final String KEY_CHRONOMETER = "chronometer";
  private static final String KEY_POLYLINE = "polyline";
  private static final String KEY_MARKER = "marker";
  private static final String KEY_TITLE = "title";
  private static final String KEY_ID = "id";

  private static final String[] PERMISSIONS_REQUIRED = {
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.CAMERA,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private PermissionHelper permissionHelper;

  public final StartVisitMap startVisitMap = new StartVisitMap(this);
  public NewVisitViewModel viewModel;
  private FragmentStartVisitBinding binding;
  private Activity activity;

  // Sensors
  private Accelerometer accelerometer;
  private AmbientSensor ambientSensor;
  private Barometer barometer;

  // A reference to the service used to get location updates.
  private StartVisitService mService = null;

  // Tracks the bound state of the service.
  private boolean bound = false;

  // Monitors the state of the connection to the service.
  private final ServiceConnection serviceConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      StartVisitService.LocalBinder binder = (StartVisitService.LocalBinder) service;
      mService = binder.getService();

      if (mService.isRunning()) {
        restoreStateFromService();
        mService.startService(StartVisitFragment.this);
      } else {
        setStateToService();
        mService.startService(StartVisitFragment.this);
      }

      bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mService = null;
      bound = false;
    }
  };

  /**
   * savedInstanceState is kept and Service is restarted. Fragment has newer data than Service, set
   * the values to use the fragment data
   */
  private void setStateToService() {
    mService.visitRowId = viewModel.getVisitRowId();
    mService.newVisitTitle = viewModel.getNewVisitTitle().getValue();
    mService.chronometerBase = viewModel.getElapsedTime();
    mService.latLngList.clear();
    mService.latLngList.addAll(startVisitMap.latLngList);
    mService.markerList.clear();
    mService.markerList.addAll(startVisitMap.markerList);
  }

  /**
   * Service did not get restarted and data is kept in the foreground service, restore the data from
   * service
   */
  private void restoreStateFromService() {
    viewModel.setVisitRowId(mService.visitRowId);
    viewModel.setNewVisitTitle(mService.newVisitTitle);
    viewModel.setElapsedTime(mService.chronometerBase);
    initChronometer();

    if (!mService.latLngList.isEmpty() && startVisitMap.latLngList.isEmpty()) {
      startVisitMap.latLngList.addAll(mService.latLngList);
    }

    if (!mService.markerList.isEmpty() && startVisitMap.markerList.isEmpty()) {
      startVisitMap.markerList.addAll(mService.markerList);
    }

    mService.getLastLocation();
  }

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
    permissionHelper = new PermissionHelper(activity, this, PERMISSIONS_REQUIRED);

    // Show exit confirmation dialog on backpress
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        onStopClick();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
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
    viewModel = new ViewModelProvider(this).get(NewVisitViewModel.class);
    accelerometer = new Accelerometer(activity);
    ambientSensor = accelerometer.getAmbientSensor();
    barometer = accelerometer.getBarometer();

    binding = FragmentStartVisitBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setFragment(this);
    binding.setViewModel(viewModel);
    binding.setUnit(PreferenceHelper.tempUnit(getContext()));
    binding.setTemperature(ambientSensor);
    binding.setPressure(barometer);

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
    outState.putLong(KEY_ID, viewModel.getVisitRowId());
    outState.putString(KEY_TITLE, viewModel.getNewVisitTitle().getValue());
    outState.putLong(KEY_CHRONOMETER, viewModel.getElapsedTime());
    outState.putParcelableArrayList(KEY_POLYLINE, startVisitMap.getLatLngList());
    outState.putParcelableArrayList(KEY_MARKER, startVisitMap.getMarkerList());
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
      viewModel.setVisitRowId(savedInstanceState.getLong(KEY_ID));
      viewModel.setNewVisitTitle(savedInstanceState.getString(KEY_TITLE));

      // Restore polyline and markers
      startVisitMap.setLatLngList(savedInstanceState.getParcelableArrayList(KEY_POLYLINE));
      startVisitMap.setMarkerList(savedInstanceState.getParcelableArrayList(KEY_MARKER));

      // Restore the chronometer time
      viewModel.setElapsedTime(savedInstanceState.getLong(KEY_CHRONOMETER));
      initChronometer();
    }
  }

  /**
   * Called when the fragment is visible to the user and actively running.
   */
  @Override
  public void onResume() {
    ((MainActivity) activity).setToolbarVisibility(false);

    if (!permissionHelper.hasCameraPermission() ||
        !permissionHelper.hasStoragePermission() ||
        !permissionHelper.hasLocationPermission()
    ) {
      Navigation.findNavController(binding.getRoot()).navigateUp();
    } else {
      accelerometer.startAccelerometerRecording();
      activity.bindService(new Intent(activity, StartVisitService.class), serviceConnection,
          Context.BIND_AUTO_CREATE);
    }

    super.onResume();
  }

  /**
   * Called when the Fragment is no longer resumed.  This is generally tied to Activity.onPause() of
   * the containing Activity's lifecycle.
   */
  @Override
  public void onPause() {
    accelerometer.stopAccelerometer();
    super.onPause();
  }

  /**
   * Called when the Fragment is no longer started.
   */
  @Override
  public void onStop() {
    if (bound) {
      // Unbind from the service. This signals to the service that this activity is no longer
      // in the foreground, and the service can respond by promoting itself to a foreground
      // service.
      activity.unbindService(serviceConnection);
      bound = false;
    }

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

    // Add marker and insert photo into the database
    if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
      String pathName = ImagePicker.getFirstImageOrNull(data).getPath();

      startVisitMap.addMarkerToCurrentLocation(mService, pathName);
      insertPhoto(pathName);
    }

    super.onActivityResult(requestCode, resultCode, data);
  }


  /**
   * Insert the uploaded or taken photo into the database
   *
   * @param pathName The file path of the photo
   */
  private void insertPhoto(String pathName) {
    Location location = startVisitMap.currentLocation.getValue();
    if (location == null) {
      return;
    }

    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());

    Photo photo;

    // Check if sensors reading are available
    if (ambientSensor.standardSensorAvailable() || barometer.standardSensorAvailable()) {
      Float temperature = ambientSensor.getSensorValue().getValue();
      Float pressure = barometer.getSensorValue().getValue();

      photo = Photo.create(0, (int) viewModel.getVisitRowId(), pathName, new Date(), point,
          new float[]{temperature == null ? 0 : temperature, pressure == null ? 0 : pressure});
    } else {
      photo = Photo.create(0, (int) viewModel.getVisitRowId(), pathName, new Date(), point, null);
    }

    viewModel.insertPhoto(photo);
  }

  /**
   * Called when the map is ready to be used.
   *
   * @param googleMap A non-null instance of a GoogleMap associated with the MapFragment or MapView
   * that defines the callback.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    startVisitMap.onMapReady(googleMap);
  }

  /**
   * Initialise the start new visit title
   */
  private void initStartNewVisitTitle() {
    // For horizontal scrolling effect, put in FrameLayout so chronometer won't reset the scroll
    binding.newVisitTitle.setSelected(true);

    // Receive the new visit title from NewVisitFragment
    if (getArguments() != null) {
      String newVisitTitle = StartVisitFragmentArgs.fromBundle(getArguments()).getNewVisitTitle();
      viewModel.setNewVisitTitle(newVisitTitle);
    }
  }

  /**
   * Initialise the chronometer
   */
  private void initChronometer() {
    Chronometer chronometer = binding.chronometer;

    if (viewModel.getElapsedTime() == null) {
      // If the elapsed time is not defined, it's a new ViewModel so set it.
      long startTime = SystemClock.elapsedRealtime();
      viewModel.setElapsedTime(startTime);
      chronometer.setBase(startTime);
    } else {
      // Otherwise the ViewModel has been retained, set the chronometer's base to the original
      // starting time.
      chronometer.setBase(viewModel.getElapsedTime());
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
   * Add permission and location services check to the click listener. If permission is granted and
   * device location is on, then zoom the map to the current location of the device
   */
  public void onMyLocationClick() {
    // Permission and location services check
    LocationHelper
        .checkDeviceLocation(activity, this, () -> mService.getLastLocation());
  }

  /**
   * Add a click listener to the Stop button, show exit confirmation dialog first. If positive
   * button is pressed, stop {@link StartVisitService} and replace current fragment with {@link
   * NewVisitFragment}
   */
  public void onStopClick() {
    AlertDialogHelper.createExitConfirmationDialog(activity, () -> {
      activity.stopService(new Intent(activity, StartVisitService.class));
      viewModel.endVisit(startVisitMap);
      Navigation.findNavController(binding.getRoot()).navigate(R.id.action_stop_visit);
    });
  }

  /**
   * Open camera to take image
   */
  public void onCameraClick() {
    ImagePicker.cameraOnly().imageDirectory("Photour").start(this);
  }

  /**
   * Open gallery to choose image
   */
  public void onGalleryClick() {
    ImagePicker.create(this)
        .folderMode(true)
        .toolbarFolderTitle("Add images to new visit")
        .single().showCamera(false)
        .start();
  }
}
