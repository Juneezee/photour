package com.photour.converter;

import androidx.room.TypeConverter;
import com.google.android.libraries.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Type converter for ArrayList<LatLng>
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class LatLngListConverter {

  /**
   * Convert ArrayList<LatLng> to comma separated string ([latitude, longitude], [...,...],...)
   *
   * @param latLngList The array of LatLng values
   * @return String The comma separated string
   */
  @TypeConverter
  public static String fromArrayList(ArrayList<LatLng> latLngList) {
    if (latLngList == null || latLngList.size() == 0) {
      return "";
    }

    final StringBuilder string = new StringBuilder();

    for (LatLng latLng : latLngList) {
      string.append("[").append(latLng.latitude).append(",");
      string.append(latLng.longitude).append("]").append(";");
    }

    // Remove the last comma
    string.setLength(string.length() - 1);

    return string.toString();
  }

  /**
   * Convert comma separated string to ArrayList<LatLng>
   *
   * @param string The comma separated string
   * @return ArrayList<LatLng> The array of LatLng values
   */
  @TypeConverter
  public static ArrayList<LatLng> toArrayList(String string) {
    if (string.isEmpty()) {
      return null;
    }

    final String[] arr = string.split(";");
    final ArrayList<LatLng> latLngList = new ArrayList<>();

    for (String s : arr) {
      // Remove square brackets and store latitude and longitude into another array
      String[] latLngString = s.replaceAll("[\\[\\]]", "").split(",");
      latLngList.add(
          new LatLng(Double.parseDouble(latLngString[0]), Double.parseDouble(latLngString[1]))
      );
    }

    return latLngList;
  }
}
