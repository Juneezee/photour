package com.photour.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.exifinterface.media.ExifInterface;
import com.photour.helper.BitmapHelper;
import java.lang.ref.WeakReference;

public class ClusterTask extends BitmapTask {

  private final WeakReference<ImageView> imageViewWeakReference;
  private final LoadMarkerListener listener;

  public ClusterTask(ImageView imageView, LoadMarkerListener listener) {
    this.imageViewWeakReference = new WeakReference<>(imageView);
    this.listener = listener;
  }

  /**
   * Decode the image into Bitmap and down sample it
   *
   * @param filepaths THe file path of photos
   * @return Bitmap A {@link Bitmap} object
   */
  @Override
  protected Bitmap doInBackground(String... filepaths) {
    try {
      final String filePath = filepaths[0];
      final ExifInterface exifInterface = new ExifInterface(filePath);

      Bitmap bitmap = exifInterface.hasThumbnail()
          ? exifInterface.getThumbnailBitmap()
          : BitmapHelper.decodeSampledBitmapFromResource(filePath, 50, 50);

      // Uncommon cases where GIFs and PNGs are not getting thumbnails after decode
      if (bitmap == null) {
        return BitmapFactory.decodeFile(filePath);
      }

      return bitmap;
    } catch (Exception e) {
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
    if (isCancelled()) {
      bitmap = null;
    }

    if (bitmap != null) {
      final ImageView imageView = imageViewWeakReference.get();
      imageView.setImageBitmap(bitmap);
      listener.markerLoaded();
    }

  }

  public interface LoadMarkerListener {

    void markerLoaded();
  }
}
