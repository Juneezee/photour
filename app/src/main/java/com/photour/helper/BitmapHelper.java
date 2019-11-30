package com.photour.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * A helper class for decoding {@link Bitmap}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class BitmapHelper {

  /**
   * Calculate the size of bitmap that will need to be reduced to to fit the given dimension.
   * Referenced Android Developer: Loading Large Bitmaps Efficiently
   *
   * @param options A {@link BitmapFactory.Options} object
   * @param reqWidth Required width of the image
   * @param reqHeight Required height of the image
   * @return int The sample size value
   * @see <a href="https://developer.android.com/topic/performance/graphics/load-bitmap"></a>
   */
  private static int calculateInSampleSize(
      BitmapFactory.Options options,
      int reqWidth,
      int reqHeight
  ) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  /**
   * Function to reduce bitmap size
   *
   * Referenced Android Developer: Loading Large Bitmaps Efficiently
   *
   * @param filepath File path of image
   * @param reqWidth required width
   * @param reqHeight required height
   * @return Bitmap the compressed bitmap
   * @see <a href="https://developer.android.com/topic/performance/graphics/load-bitmap"></a>
   */
  public static Bitmap decodeSampledBitmapFromResource(
      String filepath,
      int reqWidth,
      int reqHeight
  ) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    try {
      Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

      if (options.inSampleSize > 1) {
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filepath, options);
      }

      return bitmap;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
