package com.photour.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.photour.helper.BitmapHelper;

/**
 * A child class of {@link BitmapTask} for handling the async task for raw images
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class BitmapRawTask extends BitmapTask {

  private int reqWidth = 720;
  private int reqHeight = 960;

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
   * Set the required width for the decoded image
   *
   * @param reqWidth The required width of the decoded image
   */
  public void setReqWidth(int reqWidth) {
    this.reqWidth = reqWidth;
  }

  /**
   * Set the required height for the decoded image
   *
   * @param reqHeight The required height of the decoded image
   */
  public void setReqHeight(int reqHeight) {
    this.reqHeight = reqHeight;
  }

  /**
   * Load raw images. Down sampled the image to 720x960 (default) for better performance and prevent
   * scrolling lag
   *
   * @param filepaths File path of the images that will be processed
   * @return Bitmap bitmap of the uri
   */
  @Override
  protected Bitmap doInBackground(String... filepaths) {
    // Down sampled image if possible, otherwise render the raw image
    final Bitmap bitmap = BitmapHelper
        .decodeSampledBitmapFromResource(filepaths[0], reqWidth, reqHeight);

    return bitmap == null ? BitmapFactory.decodeFile(filepaths[0]) : bitmap;
  }
}
