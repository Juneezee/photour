package com.photour.ui.visit;

import android.graphics.Color;
import android.location.Location;
import androidx.lifecycle.MutableLiveData;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.model.JointType;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.PolylineOptions;
import java.util.ArrayList;

/**
 * A class for handling and interacting with the map object in {@link StartVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitMap implements OnMapReadyCallback {

  private final StartVisitFragment startVisitFragment;

  private GoogleMap googleMap;

  private static final int ZOOM_LEVEL = 17;

  ArrayList<LatLng> latLngList = new ArrayList<>();
  ArrayList<LatLng> markerList = new ArrayList<>();

  MutableLiveData<Location> currentLocation = new MutableLiveData<>();

  /**
   * Constructor for the class {@link StartVisitMap}
   *
   * @param startVisitFragment A {@link StartVisitFragment} fragment instance
   */
  StartVisitMap(StartVisitFragment startVisitFragment) {
    this.startVisitFragment = startVisitFragment;
  }

  /**
   * Get the array list of each location update
   *
   * @return ArrayList<LatLng> An array list of {@link LatLng} storing the LatLng of each location
   * update
   */
  ArrayList<LatLng> getLatLngList() {
    return latLngList;
  }

  /**
   * Get the array list of markers
   *
   * @return ArrayList<LatLng> An array list of {@link LatLng} storing the LatLng of each marker
   */
  ArrayList<LatLng> getMarkerList() {
    return markerList;
  }

  /**
   * Set the array list of each location update
   *
   * @param latLngList The new {@link LatLng} array list of location updates
   */
  void setLatLngList(ArrayList<LatLng> latLngList) {
    this.latLngList = latLngList;
  }

  /**
   * Set the array list of markers
   *
   * @param markerList The new {@link LatLng} array list of markers
   */
  void setMarkerList(ArrayList<LatLng> markerList) {
    this.markerList = markerList;
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
//    this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);

    // Restore polyline and markers if fragment is re-created
    drawPolyline();
    drawMarkers();

    observeCurrentLocation();
  }

  /**
   * Observe the value of <var>currentLocation</var>. Draw polyline on map and move the camera when
   * <var>currentLocation</var> has changed
   */
  private void observeCurrentLocation() {
    currentLocation.observe(startVisitFragment, location -> {
      LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
      drawPolyline();

      // First location update should not animate to prevent fast zoom on initialisation
      if (startVisitFragment.isFirstTime()) {
        startVisitFragment.setFirstTime();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
      } else {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
      }
    });
  }

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

  /**
   * Add a marker at specific location to the map
   *
   * @param point A {@link LatLng} point to add
   */
  private void addMarkerToMap(LatLng point) {
    googleMap.addMarker(new MarkerOptions().position(point));
    markerList.add(point);
  }

  /**
   * Add current location as marker to map
   */
  void addCurrentLocationAsMarker() {
    Location location = currentLocation.getValue();
    if (location == null) {
      return;
    }

    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
    googleMap.addMarker(new MarkerOptions().position(point));
    markerList.add(point);
  }

  /**
   * Draw all markers on map
   */
  private void drawMarkers() {
    final int POINTS = markerList.size();
    for (int i = 0; i < POINTS; i++) {
      addMarkerToMap(markerList.get(i));
    }
  }
}
