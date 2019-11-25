package com.android.photour.ui.visit;

import android.app.Activity;
import android.app.PendingIntent;
import android.graphics.Color;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.libraries.maps.CameraUpdate;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.model.JointType;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.PolylineOptions;
import java.util.ArrayList;

public class StartVisitMap implements OnMapReadyCallback {

  private final StartVisitFragment startVisitFragment;

  private GoogleMap googleMap;
  private FusedLocationProviderClient fusedLocationProviderClient;

  // Constants for Google Map setting and location request
  private static final int ZOOM_LEVEL = 17;
  private static final int UPDATE_INTERVAL = 20000;
  private static final int FASTEST_INTERVAL = 1000;
  private static final float MIN_DISPLACEMENT = 5;

  private ArrayList<LatLng> latLngList = new ArrayList<>();
  private ArrayList<LatLng> markerList = new ArrayList<>();

  private Location currentLocation;
  private LocationCallback locationCallback;

  private PendingIntent pendingIntent;


  /**
   * Constructor for the class {@link StartVisitMap}
   *
   * @param startVisitFragment A {@link StartVisitFragment} fragment instance
   */
  StartVisitMap(StartVisitFragment startVisitFragment) {
    this.startVisitFragment = startVisitFragment;

    setLocationCallback();
  }

  /**
   *
   */
  private void setLocationCallback() {
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);

        currentLocation = locationResult.getLastLocation();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        latLngList.add(latLng);

        drawPolyline();

        // First location update should not animate to prevent fast zoom on initialisation
        if (startVisitFragment.isFirstTime()) {
          startVisitFragment.setFirstTime();
          googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        } else {
          googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        }
      }
    };
  }

  /**
   * Set the main entry point for interacting with the fused location provider. It must be set in
   * the fragment, not this class
   *
   * @param fusedLocationProviderClient A {@link FusedLocationProviderClient} class object
   */
  void setFusedLocationProviderClient(FusedLocationProviderClient fusedLocationProviderClient) {
    this.fusedLocationProviderClient = fusedLocationProviderClient;
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
    this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);

    // Restore polyline and markers if fragment is re-created
    drawPolyline();
    drawMarkers();

    startLocationUpdates();
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
    LatLng point = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
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

  void stopLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(pendingIntent);
  }

  /**
   * Zoom the map to the current location of the device
   */
  void zoomToCurrentLocation() {
    if (startVisitFragment.getActivity() == null) {
      return;
    }

    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(startVisitFragment.getActivity(), location -> {
      if (location == null) {
        // The location is likely to be null when location services is faulty, request again
        startVisitFragment.onMyLocationClick();
      } else {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL);

        googleMap.animateCamera(cameraUpdate);
      }
    }).addOnFailureListener(e -> startVisitFragment.onMyLocationClick());
  }
}
