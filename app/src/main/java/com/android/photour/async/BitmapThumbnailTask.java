package com.android.photour.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;
import androidx.exifinterface.media.ExifInterface;
import com.android.photour.MainActivity;
import com.android.photour.helper.BitmapHelper;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Async Task that handles caching bitmaps
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class BitmapThumbnailTask extends BitmapTask {

  private static final int REQ_WIDTH = 100;
  private static final int REQ_HEIGHT = 100;

  private Uri data = null;

  /**
   * Constructor for BitmapTask
   *
   * @param context contextReference of activity
   * @param imageView imageView that the bitmap will be set on
   */
  public BitmapThumbnailTask(Context context, ImageView imageView) {
    super(context, imageView);
  }

  /**
   * Task being run async. Compresses the bitmap and crop it to 100x100. The bitmap is then saved in
   * LRU cache to used in the future.
   *
   * @param params Uri of image that will be processed
   * @return Bitmap bitmap of the uri
   */
  @Override
  protected Bitmap doInBackground(Uri... params) {
    Bitmap bitmap;
    try {
      Context contextRef = contextReference.get();
      data = params[0];

      InputStream inputStream = contextRef.getContentResolver().openInputStream(data);

      if (inputStream == null) {
        return null;
      }

      ExifInterface exifInterface = new ExifInterface(new BufferedInputStream(inputStream));
      String idStr = data.getPath().substring(data.getPath().lastIndexOf('/') + 1);

      bitmap = exifInterface.hasThumbnail()
          ? exifInterface.getThumbnailBitmap()
          : ((MainActivity)contextRef).getBitmapFromDiskCache(idStr) != null ?
              ((MainActivity)contextRef).getBitmapFromDiskCache(idStr) :
              BitmapHelper.decodeSampledBitmapFromResource(contextRef, data, REQ_WIDTH, REQ_HEIGHT);

      ((MainActivity) contextRef).addBitmapToMemoryCache(idStr, bitmap);

      return bitmap;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Checks if the task should be cancelled
   *
   * @param data Uri of image
   * @param imageView ImageView of that the task is linked on
   * @return boolean true if tasks should be cancelled, else false
   */
  public static boolean shouldCancelTask(Uri data, ImageView imageView) {
    final BitmapThumbnailTask bitmapThumbnailTask = (BitmapThumbnailTask) getBitmapTask(imageView);

    if (bitmapThumbnailTask != null) {
      final Uri bitmapData = bitmapThumbnailTask.data;
      if (bitmapData != data) {
        // Cancel previous task
        bitmapThumbnailTask.cancel(true);
      } else {
        // The same work is already in progress
        return false;
      }
    }
    // No task associated with the ImageView, or an existing task was cancelled
    return true;
  }
}
