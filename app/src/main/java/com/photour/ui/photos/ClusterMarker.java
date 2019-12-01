package com.photour.ui.photos;

import com.google.android.libraries.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Specialised marker used for the Google maps ClusterManager
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ClusterMarker implements ClusterItem {

  private final String filePath;
  private final LatLng position;

  ClusterMarker(String filePath, LatLng position) {
    this.filePath = filePath;
    this.position = position;
  }

  @Override
  public LatLng getPosition() {
    return position;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public String getSnippet() {
    return null;
  }

  public String getFilePath() {
    return filePath;
  }
}
