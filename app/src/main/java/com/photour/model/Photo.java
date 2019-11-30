package com.photour.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.exifinterface.media.ExifInterface;
import androidx.navigation.Navigation;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import com.google.android.libraries.maps.model.LatLng;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.helper.CacheHelper;
import com.photour.task.AsyncDrawable;
import com.photour.task.BitmapRawTask;
import com.photour.task.BitmapThumbnailTask;
import com.photour.ui.photos.PhotosFragmentDirections;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Entity and Model class for Photo
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@AutoValue
@Entity(tableName = "photos",
    foreignKeys = @ForeignKey(
        entity = Visit.class,
        parentColumns = "id",
        childColumns = "visitId",
        onDelete = ForeignKey.CASCADE
    )
)
public abstract class Photo implements Parcelable {

  @CopyAnnotations
  @PrimaryKey(autoGenerate = true)
  public abstract int id();

  public abstract int visitId();

  @CopyAnnotations
  @ColumnInfo(name = "file_path")
  public abstract String filePath();

  public abstract Date date();

  public abstract LatLng latLng();

  @Nullable
  public abstract float[] sensors();

  public static Photo create(
      int id,
      int visitId,
      String filePath,
      Date date,
      LatLng latLng,
      float[] sensors
  ) {
    return new AutoValue_Photo(id, visitId, filePath, date, latLng, sensors);
  }

  public boolean hasSensorsReading() {
    return sensors() != null;
  }

  public float temperatureCelsius() {
    return sensors() == null ? 0 : sensors()[0];
  }

  public float temperatureFahrenheit() {
    return sensors() == null ? 0 : (sensors()[0] * 1.8f) + 32f;
  }

  public float pressure() {
    return sensors() == null ? 0 : sensors()[1];
  }

  /**
   * Format the date into user-friendly format
   *
   * @return String The formatted date
   */
  public String getDateInString() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy • HH:mm",
        Locale.getDefault());
    return simpleDateFormat.format(date());
  }

  /**
   * Onclick listener of the image
   */
  public void onImageClick(View view) {
    Navigation.findNavController(view).navigate(PhotosFragmentDirections.actionViewPhoto(this));
  }

  /**
   * Data binding adapter for loading the thumbnail images
   *
   * @param imageView An {@link ImageView} object
   * @param filepath filepath of image
   */
  @BindingAdapter({"imageBitmap"})
  public static void loadImageBitmap(ImageView imageView, String filepath) {
    final Context context = imageView.getContext();

    Bitmap bitmap = ((MainActivity) context).cacheHelper
        .getBitmapFromMemCache(CacheHelper.getImageIdString(filepath));
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
    } else {
      if (BitmapThumbnailTask.shouldCancelTask(filepath, imageView)) {
        BitmapThumbnailTask task = new BitmapThumbnailTask(context, imageView);
        Bitmap placeholder = BitmapFactory
            .decodeResource(context.getResources(), R.drawable.placeholder);
        final AsyncDrawable asyncDrawable =
            new AsyncDrawable(context.getResources(), placeholder, task);
        imageView.setImageDrawable(asyncDrawable);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filepath);
      }
    }
  }

  /**
   * Data binding adapter for loading the raw images
   *
   * @param imageView An {@link ImageView} object
   * @param filepath filepath of image
   * @param reqWidth The required width for the decoded bitmap
   * @param reqHeight The required height for the decoded bitmap
   */
  @BindingAdapter(value = {"rawImage", "reqWidth", "reqHeight"}, requireAll = false)
  public static void loadRawImage(
      ImageView imageView,
      String filepath,
      int reqWidth,
      int reqHeight
  ) {
    final Context context = imageView.getContext();

    BitmapRawTask bitmapRawTask = new BitmapRawTask(imageView.getContext(), imageView);

    if (reqWidth != 0) {
      bitmapRawTask.setReqWidth(reqWidth);
    }

    if (reqHeight != 0) {
      bitmapRawTask.setReqHeight(reqHeight);
    }

    try {
      ExifInterface exifInterface = new ExifInterface(filepath);

      // Show the thumbnail first, then async load the raw image
      Bitmap placeholder = exifInterface.hasThumbnail()
          ? exifInterface.getThumbnailBitmap()
          : BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder);

      final AsyncDrawable asyncDrawable =
          new AsyncDrawable(context.getResources(), placeholder, bitmapRawTask);

      imageView.setImageDrawable(asyncDrawable);
      bitmapRawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filepath);
    } catch (Exception ignored) {
    }
  }
}
