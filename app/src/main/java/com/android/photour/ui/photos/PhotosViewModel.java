package com.android.photour.ui.photos;

import android.content.Context;
import android.util.DisplayMetrics;
import androidx.lifecycle.ViewModel;
import com.android.photour.ImageElement;
import com.android.photour.R;

public class PhotosViewModel extends ViewModel {

  // Statics for readwrite images
  private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
  private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
  private ImageElement img;

  public PhotosViewModel() {
    this.img = new ImageElement(R.drawable.ic_full_logo_vertical);
  }

  /**
   * Helper function to recalculate number of columns
   *
   * @param context Context of application
   * @param columnWidthDp Size of column in dp
   * @return int number of columns
   */
  int calculateNoOfColumns(Context context, float columnWidthDp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
    return (int) (screenWidthDp / columnWidthDp + 0.5);
  }

}
