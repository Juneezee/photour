package com.photour.model;

import android.os.Parcelable;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.google.android.libraries.maps.model.LatLng;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.photour.converter.LatLngListConverter;
import com.photour.ui.visits.VisitsFragmentDirections;
import com.photour.ui.visits.VisitsFragmentDirections.ActionViewVisit;
import java.util.ArrayList;
import java.util.Date;

/**
 * Entity and Model class for Visit
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@AutoValue
@Entity(tableName = "visits")
@TypeConverters({LatLngListConverter.class})
public abstract class Visit implements Parcelable {

  @CopyAnnotations
  @PrimaryKey(autoGenerate = true)
  public abstract int id();

  public abstract String visitTitle();

  public abstract Date date();

  public abstract long elapsedTime();

  @Nullable
  public abstract ArrayList<LatLng> latLngList();

  public static Visit create(
      int id,
      String visitTitle,
      Date date,
      long elapsedTime,
      ArrayList<LatLng> latLngList
  ) {
    return new AutoValue_Visit(id, visitTitle, date, elapsedTime, latLngList);
  }

  /**
   * Onclick listener of the image
   */
  public void onImageClick(View view) {
    ActionViewVisit actionViewVisit = VisitsFragmentDirections.actionViewVisit(this);
    Navigation.findNavController(view).navigate(actionViewVisit);
  }
}
