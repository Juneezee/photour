package com.android.photour.ui.visit;

import android.Manifest;
import android.Manifest.permission;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.android.photour.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import java.util.Objects;

public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private static final int PERMISSION_REQUEST_CODE = 9001;
  private static final int PLAY_SERVICES_ERROR_CODE = 9002;
  private static final String TAG = "MapDebug";
  private boolean mLocationPermissionGranted;

  private GoogleMap mGoogleMap;
  private MapView mMapView;

  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    View root = inflater.inflate(R.layout.fragment_visit_map, container, false);

    mMapView = root.findViewById(R.id.mapView);
    mMapView.onCreate(savedInstanceState);
    initGoogleMap();

    return root;
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    Log.d(TAG, "onMapReady: map is showing on the screen");

  }

  private void initGoogleMap() {
    if (isServicesOk()) {
      if (checkLocationPermission()) {
        mMapView.getMapAsync(this);
      } else {
        requestLocationPermission();
      }
    }
  }

  private boolean checkLocationPermission() {
    return ContextCompat.checkSelfPermission(
        Objects.requireNonNull(getContext()), permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED;
  }

  private boolean isServicesOk() {
    GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

    int result = googleApi.isGooglePlayServicesAvailable(this.getContext());

    if (result == ConnectionResult.SUCCESS) {
      return true;
    } else if (googleApi.isUserResolvableError(result)) {
      Dialog dialog = googleApi
          .getErrorDialog(getActivity(), result, PLAY_SERVICES_ERROR_CODE, task ->
              Toast.makeText(this.getContext(), "Dialog is cancelled by User", Toast.LENGTH_SHORT)
                  .show());
      dialog.show();
    } else {
      Toast.makeText(this.getContext(), "Play services are required by this application",
          Toast.LENGTH_SHORT).show();
    }
    return false;
  }

  private void requestLocationPermission() {
    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
        Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            PERMISSION_REQUEST_CODE);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == PERMISSION_REQUEST_CODE
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      mLocationPermissionGranted = true;
      Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getContext(), "Permission not granted", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    mMapView.onSaveInstanceState(outState);
  }

  @Override
  public void onStart() {
    super.onStart();
    mMapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mMapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mMapView.onPause();

  }

  @Override
  public void onStop() {
    super.onStop();
    mMapView.onStop();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mMapView.onDestroy();

  }
}
