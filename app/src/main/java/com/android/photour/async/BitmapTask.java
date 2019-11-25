package com.android.photour.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

public class BitmapTask extends AsyncTask<Uri, Void, Bitmap> {

  WeakReference<Context> contextReference;
  WeakReference<ImageView> imageViewWeakReference;

  /**
   * Constructor for BitmapTask
   *
   * @param context context of activity
   * @param imageView imageView that the bitmap will be set on
   */
  public BitmapTask(Context context, ImageView imageView) {
    this.contextReference = new WeakReference<>(context);
    this.imageViewWeakReference = new WeakReference<>(imageView);
  }

  @Override
  protected Bitmap doInBackground(Uri... params) { return null; }

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

      if (this == getBitmapTask(imageView)) {
        imageView.setImageBitmap(bitmap);
      }
    }
  }

  /**
   * Get the BitmapTask related to the given ImageView
   *
   * @param imageView ImageView object
   */
  static BitmapTask getBitmapTask(ImageView imageView) {

    if (imageView != null) {
      final Drawable drawable = imageView.getDrawable();
      if (drawable instanceof AsyncDrawable) {
        final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
        return asyncDrawable.getBitmapTask();
      }
    }

    return null;
  }
}
