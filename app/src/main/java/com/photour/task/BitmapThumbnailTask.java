package com.photour.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.exifinterface.media.ExifInterface;
import com.photour.MainActivity;
import com.photour.helper.BitmapHelper;
import com.photour.helper.CacheHelper;

/**
 * A child class of {@link BitmapTask} for handling the async task for thumbnails
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class BitmapThumbnailTask extends BitmapTask {

  private String filepath = null;

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
   * @param filepaths File path of photos that will be processed
   * @return Bitmap bitmap of the image
   */
  @Override
  protected Bitmap doInBackground(String... filepaths) {
    Bitmap bitmap;
    try {
      final Context contextRef = contextReference.get();
      filepath = filepaths[0];

      final ExifInterface exifInterface = new ExifInterface(filepath);
      String idStr = filepaths[1];

      /*
       * Case 1: Image is supported by ExifInterface and has thumbnail, then return embded thumbnail
       * Case 2: Image is not supported by ExifInterface but stored in disk cache, return cached
       * thumbnail
       * Else case: Decode the thumbnail manually
       *
       */
      bitmap = exifInterface.hasThumbnail()
          ? exifInterface.getThumbnailBitmap()
          : ((MainActivity) contextRef).cacheHelper.getBitmapFromDiskCache(idStr) != null
              ? ((MainActivity) contextRef).cacheHelper.getBitmapFromDiskCache(idStr)
              : BitmapHelper.decodeSampledBitmapFromResource(filepath, 100, 100);

      // Uncommon cases where GIFs and PNGs are not getting thumbnails after decode
      if (bitmap == null) {
        bitmap = BitmapFactory.decodeFile(filepath);
      }

      ((MainActivity) contextRef).cacheHelper.addBitmapToMemoryCache(idStr, bitmap);

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
  public static boolean shouldCancelTask(String data, ImageView imageView) {
    final BitmapThumbnailTask bitmapThumbnailTask = (BitmapThumbnailTask) getBitmapTask(imageView);

    // No task associated with the ImageView, or an existing task was cancelled
    if (bitmapThumbnailTask == null) {
      return true;
    }

    final String bitmapData = bitmapThumbnailTask.filepath;

    if (data != null && bitmapData != null && !bitmapData.equals(data)) {
      // Cancel previous task
      bitmapThumbnailTask.cancel(true);
      return true;
    } else {
      // The same work is already in progress
      return false;
    }
  }
}
