package com.android.photour.async;

import static com.android.photour.ui.photos.PhotoAdapter.decodeSampledBitmapFromResource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.android.photour.MainActivity;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

/**
 * Async Task that handles caching bitmaps
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {

  private WeakReference<Context> context;
  private WeakReference<ImageView> imageViewWeakReference;
  private Uri data = null;

  /**
   * Constructor for BitmapWorkerTask
   *
   * @param context context of activity
   * @param imageView imageView that the bitmap will be set on
   */
  public BitmapWorkerTask(Context context, ImageView imageView) {
    this.context = new WeakReference<>(context);
    this.imageViewWeakReference = new WeakReference<>(imageView);
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
      Context contextRef = context.get();
      data = params[0];
      bitmap = decodeSampledBitmapFromResource(contextRef, data, 100, 100);
      bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);
      ((MainActivity) contextRef).addBitmapToMemoryCache(String.valueOf(data), bitmap);
      return bitmap;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Runs on the UI thread after {@link #doInBackground}. The specified result is the value returned
   * by {@link #doInBackground}.
   *
   * Sets bitmap onto the imageView if it is not recycled.
   *
   * @param bitmap The bitmap thumbnail created
   */
  @Override
  protected void onPostExecute(Bitmap bitmap) {
    // Checks if this task is cancelled
    if (isCancelled()) {
      bitmap = null;
    }
    // If bitmap did not fail and imageView is not recycled, set the bitmap
    if (imageViewWeakReference != null && bitmap != null) {
      final ImageView imageView = imageViewWeakReference.get();
      final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
      if (this == bitmapWorkerTask) {
        imageView.setImageBitmap(bitmap);
      }
    }
  }

  /**
   * Checks if the task should be cancelled
   *
   * @param data Uri of image
   * @param imageView ImageView of that the task is linked on
   * @return boolean true if tasks should be cancelled, else false
   */
  public static boolean cancelPotentialWork(Uri data, ImageView imageView) {
    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

    if (bitmapWorkerTask != null) {
      final Uri bitmapData = bitmapWorkerTask.data;
      if (bitmapData != data) {
        // Cancel previous task
        bitmapWorkerTask.cancel(true);
      } else {
        // The same work is already in progress
        return false;
      }
    }
    // No task associated with the ImageView, or an existing task was cancelled
    return true;
  }

  /**
   * Get the BitmapWorkerTask related to the given ImageView
   *
   * @param imageView ImageView object
   */
  private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {

    if (imageView != null) {
      final Drawable drawable = imageView.getDrawable();
      if (drawable instanceof AsyncDrawable) {
        final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
        return asyncDrawable.getBitmapWorkerTask();
      }
    }

    return null;
  }
}
