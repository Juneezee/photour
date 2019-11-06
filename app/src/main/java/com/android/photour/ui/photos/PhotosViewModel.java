package com.android.photour.ui.photos;

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

}
