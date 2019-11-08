package com.android.photour.ui.visit;

import android.Manifest.permission;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.photour.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.MapView;
import com.google.android.libraries.maps.OnMapReadyCallback;
import java.util.Objects;

public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private static final int PERMISSION_REQUEST_CODE = 9001;
  private static final int PLAY_SERVICES_ERROR_CODE = 9002;
  private static final String TAG = "MapDebug";
  private boolean locationPermissionGranted;

  private GoogleMap googleMap;
  private MapView mapView;

  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    View root = inflater.inflate(R.layout.fragment_visit_map, container, false);
    stopVisitListener(root);

    mapView = root.findViewById(R.id.map_start_visit);
    mapView.onCreate(savedInstanceState);
    initGoogleMap();

    return root;
  }

  private void stopVisitListener(View root) {
    final Button stopButton = root.findViewById(R.id.button_stop_visit);

    stopButton.setOnClickListener(v -> {
      VisitFragment visitFragment = new VisitFragment();
      assert getFragmentManager() != null;
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.nav_host_fragment, visitFragment);
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction.commit();
    });
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
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
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
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();

  }

  @Override
  public void onStop() {
    super.onStop();
    mapView.onStop();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();

  }
}
