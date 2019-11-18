package com.android.photour.async;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

    /***
     *  Constructor for AsyncDrawable. Used as a reference to replace the given imageView
     *  when async task is done.
     *
     * @param res Resource object
     * @param bitmap Placeholder bitmap for the imageView
     * @param bitmapWorkerTask Async task for the given imageView
     */
    public AsyncDrawable(Resources res, Bitmap bitmap,
                         BitmapWorkerTask bitmapWorkerTask) {
        super(res, bitmap);
        bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
    }

    /***
     * Getter for BitmapWorkerTask
     *
     * @return BitmapWorkerTask
     */
    public BitmapWorkerTask getBitmapWorkerTask() {
        return bitmapWorkerTaskReference.get();
    }
}
