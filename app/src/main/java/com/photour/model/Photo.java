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
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.google.android.libraries.maps.model.LatLng;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.google.maps.android.clustering.ClusterItem;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.converter.LatLngConverter;
import com.photour.helper.DateHelper;
import com.photour.helper.StringHelper;
import com.photour.task.AsyncDrawable;
import com.photour.task.BitmapRawTask;
import com.photour.task.BitmapThumbnailTask;
import com.photour.ui.photos.PhotosFragmentDirections;
import java.util.Date;
import java.util.Objects;

/**
 * Entity and Model class for Photo
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@AutoValue
@Entity(tableName = "photos",
    indices = @Index("visitId"),
    foreignKeys = @ForeignKey(
        entity = Visit.class,
        parentColumns = "id",
        childColumns = "visitId",
        onDelete = ForeignKey.CASCADE
    )
)
@TypeConverters({LatLngConverter.class})
public abstract class Photo implements Parcelable, ClusterItem {

  @CopyAnnotations
  @PrimaryKey(autoGenerate = true)
  public abstract int id();

  @CopyAnnotations
  @ColumnInfo(name = "visitId")
  public abstract int visitId();

  @CopyAnnotations
  @ColumnInfo(name = "file_path")
  public abstract String filePath();

  @CopyAnnotations
  @ColumnInfo(name = "date")
  public abstract Date date();

  @CopyAnnotations
  @ColumnInfo(name = "latLng")
  public abstract LatLng latLng();

  @Nullable
  @CopyAnnotations
  @ColumnInfo(name = "sensors")
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

  @Override
  public LatLng getPosition() {
    return latLng();
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public String getSnippet() {
    return null;
  }

  public boolean hasSensorsReading() {
    return sensors() != null;
  }

  public float temperatureCelsius() {
    return sensors() == null ? 0 : Objects.requireNonNull(sensors())[0];
  }

  public float temperatureFahrenheit() {
    if (sensors() == null) {
      return 0;
    }

    float value = (Objects.requireNonNull(sensors())[0] * 1.8f + 32) * 10;

    // Round to 1 decimal
    return ((float) ((int) ((value - (int) value) >= 0.5f ? value + 1 : value))) / 10;
  }

  public float pressure() {
    return sensors() == null ? 0 : Objects.requireNonNull(sensors())[1];
  }

  /**
   * Format the date into user-friendly format
   *
   * @return String The formatted date
   */
  public String getDateInString() {
    return DateHelper.regularFormatWithNameTime(date());
  }

  /**
   * Onclick listener of the image
   */
  public void onImageClick(View view) {
    Navigation.findNavController(view).navigate(PhotosFragmentDirections.actionViewPhoto(this));
  }

  /**
   * Data binding adapter for loading the thumbnail photos
   *
   * @param imageView An {@link ImageView} object
   * @param filepath filepath of image
   * @param photoId The ID of the photo
   */
  @BindingAdapter({"thumbnailPhoto", "photoId"})
  public static void loadThumbnailPhoto(ImageView imageView, String filepath, int photoId) {
    final Context context = imageView.getContext();
    final String id = String.valueOf(photoId);
    Bitmap bitmap = ((MainActivity) context).cacheHelper.getBitmapFromMemCache(id);
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
      return;
    }

    if (BitmapThumbnailTask.shouldCancelTask(filepath, imageView)) {
      BitmapThumbnailTask task = new BitmapThumbnailTask(context, imageView);
      Bitmap placeholder = BitmapFactory
          .decodeResource(context.getResources(), R.drawable.placeholder);
      final AsyncDrawable asyncDrawable =
          new AsyncDrawable(context.getResources(), placeholder, task);
      imageView.setImageDrawable(asyncDrawable);
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filepath, id);
    }
  }

  /**
   * Data binding adapter for loading the raw photos
   *
   * @param imageView An {@link ImageView} object
   * @param filepath filepath of image
   * @param reqWidth The required width for the decoded bitmap
   * @param reqHeight The required height for the decoded bitmap
   */
  @BindingAdapter(value = {"rawPhoto", "reqWidth", "reqHeight"}, requireAll = false)
  public static void loadRawPhoto(
      ImageView imageView,
      String filepath,
      int reqWidth,
      int reqHeight
  ) {
    final Context context = imageView.getContext();

    // No photo, show placeholder and return
    if (StringHelper.isInvalidString(filepath)) {
      imageView.setImageBitmap(
          BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder)
      );
      return;
    }

    BitmapRawTask bitmapRawTask = new BitmapRawTask(context, imageView);
    bitmapRawTask.setReqWidth(reqWidth);
    bitmapRawTask.setReqHeight(reqHeight);

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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
