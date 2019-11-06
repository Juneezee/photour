package com.android.photour.ui.visit;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.android.photour.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import java.util.Objects;

public class StartVisitActivity extends FragmentActivity implements OnMapReadyCallback {

  private GoogleMap mMap;
  private CameraPosition mCameraPosition;

  // The entry point to the Fused Location Provider.
  private FusedLocationProviderClient mFusedLocationProviderClient;

  // A default location (Sheffield, UK) and default zoom to use when location permission is
  // not granted.
  private final LatLng mDefaultLocation = new LatLng(53.3809, 1.4879);
  private static final int DEFAULT_ZOOM = 15;
  private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
  private boolean mLocationPermissionGranted;

  // The geographical location where the device is currently located. That is, the last-known
  // location retrieved by the Fused Location Provider.
  private Location mLastKnownLocation;

  // Keys for storing activity state.
  private static final String KEY_CAMERA_POSITION = "camera_position";
  private static final String KEY_LOCATION = "location";

  /**
   * Perform the required actions when the activity is created
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   * down then this Bundle contains the data it most recently supplied in
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Retrieve location and camera position from saved instance state.
    if (savedInstanceState != null) {
      mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
      mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
    }

    setContentView(R.layout.fragment_visit_map);

    // Construct a FusedLocationProviderClient.
    mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    assert mapFragment != null;
    mapFragment.getMapAsync(this);
  }

  /**
   * Saves the state of the map when the activity is paused.
   *
   * @param outState Bundle object to pass data
   */
  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    if (mMap != null) {
      outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
      outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
      super.onSaveInstanceState(outState);
    }
  }

  /**
   * Manipulates the map when it's available. This callback is triggered when the map is ready to be
   * used.
   *
   * @param googleMap The Google Map object
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Prompt the user for permission.
    getLocationPermission();

    // Turn on the My Location layer and the related control on the map.
    updateLocationUI();

    // Get the current location of the device and set the position of the map.
    getDeviceLocation();
  }

  /**
   * Gets the current location of the device, and positions the map's camera.
   */
  private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    try {
      if (mLocationPermissionGranted) {
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
          if (location == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
          } else {
            mLastKnownLocation = location;

            mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                    DEFAULT_ZOOM));
          }
        });
      }
    } catch (SecurityException e) {
      Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
    }
  }

  /**
   * Prompts the user for permission to use the device location.
   */
  private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    mLocationPermissionGranted = ContextCompat.checkSelfPermission(this.getApplicationContext(),
        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    if (!mLocationPermissionGranted) {
      ActivityCompat.requestPermissions(this,
          new String[]{permission.ACCESS_FINE_LOCATION},
          PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }
  }

  /**
   * Handles the result of the request for location permissions.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    mLocationPermissionGranted =
        requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED;

    // If request is cancelled, the result arrays are empty.
    if (mLocationPermissionGranted) {
      updateLocationUI();
    }
  }

  /**
   * Updates the map's UI settings based on whether the user has granted location permission.
   */
  private void updateLocationUI() {
    if (mMap == null) {
      return;
    }
    try {
      if (mLocationPermissionGranted) {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
      } else {
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mLastKnownLocation = null;
        getLocationPermission();
      }
    } catch (SecurityException e) {
      Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
    }
  }
}
