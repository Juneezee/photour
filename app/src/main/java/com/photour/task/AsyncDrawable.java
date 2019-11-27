package com.photour.task;

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

  private final WeakReference<BitmapTask> bitmapTaskReference;

  /**
   * Constructor for AsyncDrawable. Used as a reference to replace the given imageView when async
   * task is done.
   *
   * @param res Resource object
   * @param bitmap Placeholder bitmap for the imageView
   * @param bitmapTask Async task for the given imageView
   */
  public AsyncDrawable(Resources res, Bitmap bitmap, BitmapTask bitmapTask) {
    super(res, bitmap);
    bitmapTaskReference = new WeakReference<>(bitmapTask);
  }

  /**
   * Getter for BitmapThumbnailTask
   *
   * @return BitmapThumbnailTask
   */
  BitmapTask getBitmapTask() {
    return bitmapTaskReference.get();
  }
}
