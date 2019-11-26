package com.photour.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import androidx.exifinterface.media.ExifInterface;
import androidx.navigation.Navigation;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.async.AsyncDrawable;
import com.photour.async.BitmapRawTask;
import com.photour.async.BitmapTask;
import com.photour.async.BitmapThumbnailTask;
import com.photour.ui.photos.PhotosFragmentDirections;
import com.photour.ui.photos.PhotosFragmentDirections.ActionViewImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Entity for Image Element
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Entity(tableName = "image_element")
public class ImageElement implements Parcelable {

  @PrimaryKey(autoGenerate = true)
  public int id;

  @ColumnInfo(name = "relative_path")
  private String path;

  @ColumnInfo(name = "visit_title")
  private String visitTitle;

  @ColumnInfo(name = "date")
  private Date date;

  @ColumnInfo(name = "latitude")
  private double lat;

  @ColumnInfo(name = "longtitude")
  private double lng;

  @ColumnInfo(name = "pressure")
  private float pressure;

  @ColumnInfo(name = "temperature")
  private float temperature;

  /**
   * Constructor for ImageElement
   *
   * @param path File path of the image
   * @param visitTitle String of visit title
   * @param lat latitude double
   * @param lng longtitude double
   * @param pressure pressure float
   * @param temperature temperature float
   */
  public ImageElement(
      String path,
      String visitTitle,
      double lat,
      double lng,
      float pressure,
      float temperature,
      Date date
  ) {
    this.path = path;
    this.visitTitle = visitTitle;
    this.lat = lat;
    this.lng = lng;
    this.pressure = pressure;
    this.temperature = temperature;
    this.date = date;
  }

  /**
   * Constructor for allowing {@link Parcelable}
   *
   * @param in A {@link Parcel} object
   */
  protected ImageElement(Parcel in) {
    id = in.readInt();
    path = in.readString();
    visitTitle = in.readString();
    lat = in.readDouble();
    lng = in.readDouble();
    pressure = in.readFloat();
    temperature = in.readFloat();
  }

  // Auto-generated, required for passing ImageElement object between navigation
  public static final Creator<ImageElement> CREATOR = new Creator<ImageElement>() {
    @Override
    public ImageElement createFromParcel(Parcel in) {
      return new ImageElement(in);
    }

    @Override
    public ImageElement[] newArray(int size) {
      return new ImageElement[size];
    }
  };

  /**
   * Data binding adapter for loading the thumbnail images
   *
   * @param imageView An {@link ImageView} object
   * @param filepath filepath of image
   */
  @BindingAdapter({"imageBitmap"})
  public static void loadImageBitmap(ImageView imageView, String filepath) {
    final Context context = imageView.getContext();
    Bitmap bitmap = ((MainActivity) context).getBitmapFromMemCache(filepath);
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
        // task.execute(filepath);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filepath);
      }
    }
  }

  /**
   * Data binding adapter for loading the raw images
   *
   * @param imageView An {@link ImageView} object
   * @param filepath filepath of image
   */
  @BindingAdapter({"rawImage"})
  public static void loadRawImage(ImageView imageView, String filepath) {
    final Context context = imageView.getContext();

    BitmapTask bitmapRawTask = new BitmapRawTask(imageView.getContext(), imageView);

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

  /**
   * Getter for Path
   *
   * @return path String
   */
  public String getPath() {
    return path;
  }

  /**
   * Setter for Path
   *
   * @param path String
   */

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Getter for Date
   *
   * @return date Date of image
   */
  public Date getDate() {
    return date;
  }

  public String getDateInString() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy • HH:mm",
        Locale.getDefault());
    return simpleDateFormat.format(date);
  }

  /**
   * Getter for Trip Name
   *
   * @return visitTitle the name of the trip the image is in
   */
  public String getVisitTitle() {
    return visitTitle;
  }

  /**
   * Accessor for Trip Name
   *
   * @param visitTitle string
   */
  public void setVisitTitle(String visitTitle) {
    this.visitTitle = visitTitle;
  }

  /**
   * Getter for Latitude
   *
   * @return double latitude
   */
  public double getLat() {
    return lat;
  }

  /**
   * Getter for Longtitude
   *
   * @return double longitude
   */
  public double getLng() {
    return lng;
  }

  /**
   * Getter for Barometer
   *
   * @return float pressure
   */
  public float getPressure() {
    return pressure;
  }

  /**
   * Getter for Ambient
   *
   * @return float temperature
   */
  public float getTemperature() {
    return temperature;
  }

  /**
   * Setter for date
   *
   * @param date New date
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Setter for latitude
   *
   * @param lat New latitude
   */
  public void setLat(double lat) {
    this.lat = lat;
  }

  /**
   * Setter for longitude
   *
   * @param lng New longitude
   */
  public void setLng(double lng) {
    this.lng = lng;
  }

  public void setPressure(float pressure) {
    this.pressure = pressure;
  }

  public void setTemperature(float temperature) {
    this.temperature = temperature;
  }

  /**
   * Onclick listener of the image
   */
  public void onImageClick(View view) {
    ActionViewImage actionViewImage = PhotosFragmentDirections.actionViewImage(this);
    Navigation.findNavController(view).navigate(actionViewImage);
  }

  /**
   * Describe the kinds of special objects contained in this Parcelable instance's marshaled
   * representation. For example, if the object will include a file descriptor in the output of
   * {@link #writeToParcel(Parcel, int)}, the return value of this method must include the {@link
   * #CONTENTS_FILE_DESCRIPTOR} bit.
   *
   * @return a bitmask indicating the set of special object types marshaled by this Parcelable
   * object instance.
   */
  @Override
  public int describeContents() {
    return 0;
  }

  /**
   * Flatten this object in to a Parcel.
   *
   * @param dest The Parcel in which the object should be written.
   * @param flags Additional flags about how the object should be written. May be 0 or {@link
   * #PARCELABLE_WRITE_RETURN_VALUE}.
   */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(path);
    dest.writeString(visitTitle);
    dest.writeDouble(lat);
    dest.writeDouble(lng);
    dest.writeFloat(pressure);
    dest.writeFloat(temperature);
  }
}
