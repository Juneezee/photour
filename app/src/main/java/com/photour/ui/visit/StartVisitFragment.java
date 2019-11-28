package com.photour.ui.visit;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.util.Log;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import com.google.android.gms.location.LocationServices;
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
import com.photour.helper.ReceiverHelper;
import com.photour.sensor.Accelerometer;
import com.photour.service.StartVisitService;
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

  private static final String TAG = StartVisitFragment.class.getSimpleName();

//  public static final int REQUEST_CHECK_SETTINGS = 214;
  private static final String[] PERMISSIONS_REQUIRED = {
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  private PermissionHelper permissionHelper;

  private StartVisitMap startVisitMap;
  private VisitViewModel visitViewModel;
  private FragmentStartVisitBinding binding;
  private Activity activity;

  // Sensors
  private Accelerometer accelerometer;

  // JobService related
  private JobScheduler scheduler;
  private FragmentReceiver receiver;

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
    receiver = new FragmentReceiver();
    scheduler = (JobScheduler) activity.getSystemService(JOB_SCHEDULER_SERVICE);

    startVisitMap = new StartVisitMap(this,
        LocationServices.getFusedLocationProviderClient(activity));

    System.out.println("running onCreate");

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
    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
    accelerometer = new Accelerometer(activity);

    binding = FragmentStartVisitBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setFragment(this);
    binding.setViewModel(visitViewModel);
    binding.setTemperature(accelerometer.getAmbientSensor());
    binding.setPressure(accelerometer.getBarometer());

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
   * Called when the fragment is visible to the user and actively running.
   */
  @Override
  public void onResume() {
    accelerometer.startAccelerometerRecording();
    ((MainActivity) activity).setToolbarVisibility(false);
    super.onResume();

    if (!permissionHelper.hasCameraPermission() ||
        !permissionHelper.hasStoragePermission() ||
        !permissionHelper.hasLocationPermission()) {
      Navigation.findNavController(binding.getRoot()).navigateUp();
    }
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
    super.onStop();
    ((MainActivity) activity).setToolbarVisibility(true);
  }

  /**
   * Called when the fragment is no longer in use.  This is called after {@link #onStop()} and
   * before {@link #onDetach()}.
   *
   * Unregister the broadcast receiver when the user or system kills the application
   */
  @Override
  public void onDestroy() {
    ReceiverHelper.unregisterBroadcastReceiver(activity, receiver);
    super.onDestroy();
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

//        startVisitMap.addMarkerToCurrentLocation();

        System.out.println("hi");
        System.out.println(imageFiles);
        System.out.println(source);
        System.out.println(type);
        System.out.println("yo");
      }
    });
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

    ReceiverHelper
        .registerBroadcastReceiver(activity, receiver, StartVisitService.ACTION_BROADCAST);

    // If job service is running, restore to the last state before the app is stopped
    System.out.println("Is service running? " + ((MainActivity) activity).isJobServiceRunning());

    if (((MainActivity) activity).isJobServiceRunning()) {
      Intent intent = new Intent(StartVisitService.ACTION_LAUNCH);

      // Case: fragment instance state is saved and JobService is restarted.
      // So fragment has newer data than JobService
      if (!startVisitMap.getLatLngList().isEmpty()) {
        intent.putExtra(StartVisitService.EXTRA_LAUNCH, startVisitMap.latLngList);
        intent.putExtra(StartVisitService.EXTRA_CHRONOMETER, visitViewModel.getElapsedTime());
      }

      LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    } else {
      startJobService();
    }
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
   * Add permission and location services check to the click listener. If permission is granted and
   * device location is on, then zoom the map to the current location of the device
   */
  public void onMyLocationClick() {
    // Permission and location services check
    LocationHelper
        .checkDeviceLocation(activity, this, () -> startVisitMap.getLastLocation());
  }

  /**
   * Add a click listener to the Stop button, show exit confirmation dialog first. If positive
   * button is pressed, stop {@link StartVisitService} and replace current fragment with {@link
   * VisitFragment}
   */
  public void onStopClick() {
    AlertDialogHelper.createExitConfirmationDialog(activity, () -> {
      scheduler.cancel(StartVisitService.JOB_ID);
      Navigation.findNavController(binding.getRoot()).navigate(R.id.action_stop_visit);
    });
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

  /**
   * Start the {@link StartVisitService} JobService
   */
  private void startJobService() {
    ComponentName componentName = new ComponentName(activity, StartVisitService.class);
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString("title", visitViewModel.getNewVisitTitle().getValue());
    JobInfo info = new JobInfo.Builder(StartVisitService.JOB_ID, componentName)
        .setOverrideDeadline(0)
        .setExtras(bundle)
        .setPersisted(true)
        .build();

    int resultCode = scheduler.schedule(info);
    if (resultCode == JobScheduler.RESULT_SUCCESS) {
      Log.d(TAG, "Job scheduled");
    } else {
      Log.d(TAG, "Job scheduling failed");
    }
  }

  /**
   * Inner {@link BroadcastReceiver} class to handle the location sent by {@link StartVisitService}
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class FragmentReceiver extends BroadcastReceiver {

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
      Location location = intent.getParcelableExtra(StartVisitService.EXTRA_LOCATION);

      ArrayList<LatLng> latLngList = intent
          .getParcelableArrayListExtra(StartVisitService.EXTRA_LAUNCH);

      final long elapsedTime = intent
          .getLongExtra(StartVisitService.EXTRA_CHRONOMETER, SystemClock.elapsedRealtime());

      if (isApplicationRelaunch(latLngList)) {
        Log.d(TAG, "Relaunched, restoring state...");
        visitViewModel.setElapsedTime(elapsedTime);
        initChronometer();

        startVisitMap.latLngList.addAll(latLngList);
        startVisitMap.getLastLocation();
      } else if (location != null) {
        Log.d(TAG, "Received LatLng");
        startVisitMap.currentLocation.setValue(location);
      } else {
        startVisitMap.getLastLocation();
      }
    }

    /**
     * Check if the application is killed while a visit is ongoing, and then relaunched
     *
     * @param latLngList An ArrayList of LatLng stored by the service
     * @return boolean {@code true} If the application is relaunched
     */
    private boolean isApplicationRelaunch(ArrayList<LatLng> latLngList) {
      return latLngList != null && !latLngList.isEmpty() && startVisitMap.latLngList.isEmpty();
    }
  }
}
