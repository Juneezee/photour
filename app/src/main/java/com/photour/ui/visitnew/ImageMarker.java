package com.photour.ui.visitnew;

import android.os.Parcelable;
import com.google.android.libraries.maps.model.LatLng;
import com.google.auto.value.AutoValue;

/**
 * A class for storing the file path of an uploaded / taken image with a LatLng point. With help
 * from {@link AutoValue}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@AutoValue
public abstract class ImageMarker implements Parcelable {

  public abstract String imagePath();

  public abstract LatLng latLng();

  public static ImageMarker create(String imagePath, LatLng latLng) {
    return new AutoValue_ImageMarker(imagePath, latLng);
  }
}
