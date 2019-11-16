package com.android.photour;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.android.photour.ui.photos.ImageAdapter;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import static com.android.photour.ui.photos.ImageAdapter.decodeSampledBitmapFromResource;

public class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {

    Context context;
    WeakReference<ImageView> imageViewWeakReference;
    private Uri data = null;

    public BitmapWorkerTask(Context context, ImageView imageView) {
        this.context = context;
        this.imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        Bitmap bitmap;
        try {
            data = params[0];
            bitmap = decodeSampledBitmapFromResource(
                    context, data, 100, 100);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap,360,360);
            ((MainActivity)context).addBitmapToMemoryCache(String.valueOf(data), bitmap);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (imageViewWeakReference != null && bitmap != null) {
            final ImageView imageView = imageViewWeakReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

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