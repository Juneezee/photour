package com.android.photour.model;

import static android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.view.View;
import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import androidx.exifinterface.media.ExifInterface;
import androidx.navigation.Navigation;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.android.photour.async.AsyncDrawable;
import com.android.photour.async.BitmapRawTask;
import com.android.photour.async.BitmapTask;
import com.android.photour.async.BitmapThumbnailTask;
import com.android.photour.ui.photos.PhotosFragmentDirections;
import com.android.photour.ui.photos.PhotosFragmentDirections.ActionViewImage;
import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

  @ColumnInfo(name = "uri")
  private String uri;

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
   * @param uri String of Uri of the image
   * @param visitTitle String of visit title
   * @param lat latitude double
   * @param lng longtitude double
   * @param pressure pressure float
   * @param temperature temperature float
   */
  public ImageElement(String uri, String visitTitle, double lat,
      double lng, float pressure, float temperature, Date date) {
    this.uri = uri;
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
    uri = in.readString();
    visitTitle = in.readString();
    lat = in.readFloat();
    lng = in.readFloat();
    pressure = in.readFloat();
    temperature = in.readFloat();
  }

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
   * Function to load images for data binding
   *
   * @param imageView ImageView object
   * @param uri Uri of image
   */
  @BindingAdapter({"imageBitmap"})
  public static void loadImageBitmap(ImageView imageView, String uri) {
    final Context context = imageView.getContext();
    Bitmap bitmap = ((MainActivity) context).getBitmapFromMemCache(uri);
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
    } else {
      if (BitmapThumbnailTask.shouldCancelTask(Uri.parse(uri), imageView)) {
        BitmapThumbnailTask task = new BitmapThumbnailTask(context, imageView);
        Bitmap placeholder = BitmapFactory
            .decodeResource(context.getResources(), R.drawable.placeholder);
        final AsyncDrawable asyncDrawable =
            new AsyncDrawable(context.getResources(), placeholder, task);
        imageView.setImageDrawable(asyncDrawable);
        task.execute(Uri.parse(uri));
      }
    }
  }

  @BindingAdapter({"rawImage"})
  public static void loadRawImage(ImageView imageView, String uri) {
    final Context context = imageView.getContext();

    BitmapTask bitmapRawTask = new BitmapRawTask(imageView.getContext(), imageView);
    Bitmap placeholder;

    try {
      InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uri));
      ExifInterface exifInterface = new ExifInterface(new BufferedInputStream(inputStream));

      if (exifInterface.hasThumbnail()) {
        placeholder = exifInterface.getThumbnailBitmap();
      } else {
        placeholder = BitmapFactory
            .decodeResource(context.getResources(), R.drawable.placeholder);
      }

      final AsyncDrawable asyncDrawable =
          new AsyncDrawable(context.getResources(), placeholder, bitmapRawTask);

      imageView.setImageDrawable(asyncDrawable);
      bitmapRawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Uri.parse(uri));
    } catch (Exception ignored) {
    }
  }

  /**
   * Getter for Uri
   *
   * @return uri String
   */
  public String getUri() {
    return uri;
  }

  /**
   * Accessor for Uri
   *
   * @param uri String of uri
   */
  public void setUri(String uri) {
    this.uri = uri;
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
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy • HH:mm", Locale.getDefault());
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
   * @return lat latitude float
   */
  public double getLat() {
    return lat;
  }

  /**
   * Getter for Longtitude
   *
   * @return lng longtitude float
   */
  public double getLng() {
    return lng;
  }

  /**
   * Getter for Barometer
   *
   * @return pressure float
   */
  public float getPressure() {
    return pressure;
  }

  /**
   * Getter for Ambient
   *
   * @return temperature
   */
  public float getTemperature() {
    return temperature;
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
    dest.writeString(uri);
    dest.writeString(visitTitle);
    dest.writeDouble(lat);
    dest.writeDouble(lng);
    dest.writeFloat(pressure);
    dest.writeFloat(temperature);
  }
}
