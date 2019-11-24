package com.android.photour.async;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import java.lang.ref.WeakReference;

/**
 * A class for referencing tasks with ImageView
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class AsyncDrawable extends BitmapDrawable {

  private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

  /**
   * Constructor for AsyncDrawable. Used as a reference to replace the given imageView when async
   * task is done.
   *
   * @param res Resource object
   * @param bitmap Placeholder bitmap for the imageView
   * @param bitmapWorkerTask Async task for the given imageView
   */
  public AsyncDrawable(
      Resources res,
      Bitmap bitmap,
      BitmapWorkerTask bitmapWorkerTask
  ) {
    super(res, bitmap);
    bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
  }

  /**
   * Getter for BitmapWorkerTask
   *
   * @return BitmapWorkerTask
   */
  BitmapWorkerTask getBitmapWorkerTask() {
    return bitmapWorkerTaskReference.get();
  }
}
