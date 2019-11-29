package com.photour.model;

import android.os.Parcelable;
import android.view.View;
import androidx.navigation.Navigation;
import androidx.room.ColumnInfo;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.photour.ui.visits.VisitsFragmentDirections;
import com.photour.ui.visits.VisitsFragmentDirections.ActionViewVisit;

/**
 * Model class for Visit
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@AutoValue
public abstract class Visit implements Parcelable {

  @CopyAnnotations
  @ColumnInfo(name = "visit_title")
  public abstract String visitTitle();

  @CopyAnnotations
  @ColumnInfo(name = "file_path")
  public abstract String visitCover();

  @CopyAnnotations
  @ColumnInfo(name = "total_photos")
  public abstract int totalPhotos();

  public static Visit create(String visitTitle, String visitCover, int totalPhotos) {
    return new AutoValue_Visit(visitTitle, visitCover, totalPhotos);
  }

  /**
   * Onclick listener of the image
   */
  public void onImageClick(View view) {
    ActionViewVisit actionViewVisit = VisitsFragmentDirections.actionViewVisit(this);
    Navigation.findNavController(view).navigate(actionViewVisit);
  }
}
