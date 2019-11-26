package com.photour.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.photour.helper.BitmapHelper;

public class BitmapRawTask extends BitmapTask {

  /**
   * Constructor for BitmapRawTask
   *
   * @param context contextReference of activity
   * @param imageView imageView that the bitmap will be set on
   */
  public BitmapRawTask(Context context, ImageView imageView) {
    super(context, imageView);
  }

  /**
   * Load raw images. Down sampled the image to 960x720 for better performance and prevent scrolling
   * lag
   *
   * @param filepaths File path of the images that will be processed
   * @return Bitmap bitmap of the uri
   */
  @Override
  protected Bitmap doInBackground(String... filepaths) {
    // Down sampled image if possible, otherwise render the raw image
    final Bitmap bitmap = BitmapHelper.decodeSampledBitmapFromResource(filepaths[0], 720, 960);

    return bitmap == null ? BitmapFactory.decodeFile(filepaths[0]) : bitmap;
  }
}
