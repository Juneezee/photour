package com.android.photour.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.android.photour.async.AsyncDrawable;
import com.android.photour.async.BitmapWorkerTask;

import java.util.Date;

/**
 * Entity for Image Element
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Entity(tableName = "image_element")
public class ImageElement {

  @PrimaryKey(autoGenerate = true)
  public int id;
  @ColumnInfo(name = "uri")
  private String uri;
  @ColumnInfo(name = "trip_name")
  private String tripName;
  @ColumnInfo(name = "date")
  private Date date;
  @ColumnInfo(name = "latitude")
  private float lat;
  @ColumnInfo(name = "longtitude")
  private float lng;
  @ColumnInfo(name="barometer")
  private float barometer;
  @ColumnInfo(name="ambient")
  private float ambient;

  /**
   * Constructor for ImageElement
   *
   * @param uri String of Uri of the image
   * @param tripName String of trip name
   * @param lat latitude float
   * @param lng longtitude float
   * @param barometer barometer float
   * @param ambient ambient float
   */
  public ImageElement(String uri, String tripName, float lat,
                      float lng, float barometer, float ambient) {
    this.uri = uri;
    this.tripName = tripName;
    this.lat = lat;
    this.lng = lng;
    this.barometer = barometer;
    this.ambient = ambient;
    this.date = new Date();
  }

  /**
   * Getter for Uri
   * @return uri String
   */
  public String getUri() { return uri;
  }

  /**
   * Accessor for Uri
   * @param uri String of uri
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Getter for Date
   * @return date Date of image
   */
  public Date getDate() {
    return date;
  }

  /**
   * Accessor for Date
   * @param date Date
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Getter for Trip Name
   * @return tripName the name of the trip the image is in
   */
  public String getTripName() {
    return tripName;
  }

  /**
   * Accessor for Trip Name
   * @param tripName string
   */
  public void setTripName(String tripName) {
    this.tripName = tripName;
  }

  /**
   * Getter for Latitude
   * @return lat latitude float
   */
  public float getLat() {
    return lat;
  }

  /**
   * Accessor for Latitude
   * @param lat float
   */
  public void setLat(float lat) {
    this.lat = lat;
  }

  /**
   * Getter for Longtitude
   * @return lng longtitude float
   */
  public float getLng() {
    return lng;
  }

  /**
   * Accessor for Longtitude
   * @param lng float
   */
  public void setLng(float lng) {
    this.lng = lng;
  }

  /**
   * Getter for Barometer
   * @return barometer float
   */
  public float getBarometer() {
    return barometer;
  }

  /**
   * Accessor for Barometer
   * @param barometer float
   */
  public void setBarometer(float barometer) {
    this.barometer = barometer;
  }

  /**
   * Getter for Ambient
   * @return ambient
   */
  public float getAmbient() {
    return ambient;
  }

  /**
   * Accessor for Ambient
   * @param ambient float
   */
  public void setAmbient(float ambient) {
    this.ambient = ambient;
  }

  /**
   * Function to load images for data binding
   *
   * @param imageView ImageView object
   * @param uri Uri of image
   */
  @BindingAdapter({ "avatar" })
  public static void loadImage(ImageView imageView, String uri) {
    final Context context = imageView.getContext();
    Bitmap bitmap = ((MainActivity) context).getBitmapFromMemCache(uri);
    if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
      } else {
        if (BitmapWorkerTask.cancelPotentialWork(Uri.parse(uri), imageView)) {
          BitmapWorkerTask task = new BitmapWorkerTask(context, imageView);
          Bitmap placeholder = BitmapFactory
                  .decodeResource(context.getResources(), R.drawable.placeholder);
          final AsyncDrawable asyncDrawable =
              new AsyncDrawable(context.getResources(), placeholder, task);
          imageView.setImageDrawable(asyncDrawable);
          task.execute(Uri.parse(uri));
        }
      }
  }
//  imageView.setOnClickListener(view -> {
//        // INSERT CODE TO ENTER IMAGE HERE
//      });
}
