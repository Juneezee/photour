package com.photour.ui.visitnew;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.MutableLiveData;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.libraries.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.model.JointType;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.Marker;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.photour.R;
import com.photour.helper.BitmapHelper;
import com.photour.helper.LocationHelper;
import com.photour.service.StartVisitService;
import java.util.ArrayList;

/**
 * A class for handling and interacting with the map object in {@link StartVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitMap implements OnMapReadyCallback, OnMarkerClickListener {

  private final StartVisitFragment startVisitFragment;

  private GoogleMap googleMap;

  private static final int ZOOM_LEVEL = 17;

  // To check if this created the first time in current activity
  private boolean isFirstTime = true;

  ArrayList<LatLng> latLngList = new ArrayList<>();

  ArrayList<ImageMarker> markerList = new ArrayList<>();

  private String clickedMarkerImagePath;

  public final MutableLiveData<Location> currentLocation = new MutableLiveData<>();

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
    this.googleMap.getUiSettings().setMapToolbarEnabled(false);
    this.googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    this.googleMap.setOnMarkerClickListener(this);

    // Restore polyline and markers if fragment is re-created
    drawPolyline();
    drawMarkers();

    observeCurrentLocation();
  }

  /**
   * Called when a marker has been clicked or tapped.
   *
   * @param marker The marker that was clicked.
   * @return {@code true} if the listener has consumed the event (i.e., the default behavior should
   * not occur); false otherwise (i.e., the default behavior should occur). The default behavior is
   * for the camera to move to the marker and an info window to appear.
   */
  @Override
  public boolean onMarkerClick(Marker marker) {
    this.clickedMarkerImagePath = marker.getTitle();

    // Return false to indicate that we have not consumed the event and that we wish
    // for the default behavior to occur (which is for the camera to move such that the
    // marker is centered and for the marker's info window to open, if it has one).
    return false;
  }

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
  ArrayList<ImageMarker> getMarkerList() {
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
  void setMarkerList(ArrayList<ImageMarker> markerList) {
    this.markerList = markerList;
  }

  /**
   * Observe the value of <var>currentLocation</var>. Draw polyline on map and move the camera when
   * <var>currentLocation</var> has changed
   */
  private void observeCurrentLocation() {
    currentLocation.observe(startVisitFragment, location -> {
      LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

      if (LocationHelper.shouldAddToLatLntList(latLngList, latLng)) {
        latLngList.add(latLng);
      }

      drawPolyline();

      // First location update should not animate to prevent fast zoom on initialisation
      if (isFirstTime) {
        isFirstTime = false;
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
   * Add a clickable marker to current location, with photo as info content when clicked
   *
   * @param mService A binding instance of {@link StartVisitService}
   * @param pathName The file path name of the photo
   */
  void addMarkerToCurrentLocation(StartVisitService mService, String pathName) {
    Location location = currentLocation.getValue();
    if (location == null) {
      return;
    }

    // Add marker to map
    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
    ImageMarker imageMarker = ImageMarker.create(pathName, point);
    googleMap.addMarker(new MarkerOptions().position(point).title(pathName));

    markerList.add(imageMarker);
    mService.markerList.add(imageMarker);
  }

  /**
   * Draw all markers on map
   */
  private void drawMarkers() {
    for (ImageMarker marker : markerList) {
      googleMap.addMarker(new MarkerOptions().position(marker.latLng()).title(marker.imagePath()));
    }
  }

  /**
   * An adapter class for custom info window of Google Maps markers
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class CustomInfoWindowAdapter implements InfoWindowAdapter {

    @Override
    public View getInfoWindow(Marker marker) {
      return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
      Activity activity = startVisitFragment.getActivity();
      View view = View.inflate(activity, R.layout.custom_info_window, null);

      if (activity == null) {
        return null;
      }

      ImageView imageView = view.findViewById(R.id.info_image);

      Bitmap bitmap;

      // Async task won't work because the view is returned before the task is completed
      try {
        ExifInterface exifInterface = new ExifInterface(clickedMarkerImagePath);
        bitmap = exifInterface.hasThumbnail()
            ? exifInterface.getThumbnailBitmap()
            : BitmapHelper.decodeSampledBitmapFromResource(clickedMarkerImagePath, 100, 100);

      } catch (Exception e) {
        imageView.setImageResource(R.drawable.placeholder);
        return view;
      }

      imageView.setImageBitmap(bitmap);
      return view;
    }
  }
}
