package com.android.photour.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
  public static int calculateInSampleSize(
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
      while ((halfHeight / inSampleSize) >= reqHeight
          && (halfWidth / inSampleSize) >= reqWidth) {
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
   * @param context Context of MainActivity
   * @param resUri Uri of the image
   * @param reqWidth required width
   * @param reqHeight required height
   * @return Bitmap the compressed bitmap
   * @throws FileNotFoundException thrown if Uri for image is invalid
   * @see <a href="https://developer.android.com/topic/performance/graphics/load-bitmap"></a>
   */
  public static Bitmap decodeSampledBitmapFromResource(
      Context context,
      Uri resUri,
      int reqWidth,
      int reqHeight
  ) throws FileNotFoundException {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    try {
      InputStream inputStream = context.getContentResolver().openInputStream(resUri);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

      if (options.inSampleSize > 1) {
        inputStream = context.getContentResolver().openInputStream(resUri);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
      }

      if (inputStream != null) {
        inputStream.close();
      }

      return bitmap;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
