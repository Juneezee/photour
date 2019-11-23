package com.android.photour.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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

@Entity
public class ImageElement {

  @PrimaryKey
  public int uid;
  @ColumnInfo(name = "uri")
  private Uri uri;
  @ColumnInfo(name = "trip_name")
  private String tripName;
  @ColumnInfo(name = "latitude")
  private float lat;
  @ColumnInfo(name = "longtitude")
  private float lng;
  @ColumnInfo(name="barometer")
  private float barometer;
  @ColumnInfo(name="ambient")
  private float ambient;

  public ImageElement(Uri uri, String tripName, float lat,
                      float lng, float barometer, float ambient) {
    this.uri = uri;
    this.tripName = tripName;
    this.lat = lat;
    this.lng = lng;
    this.barometer = barometer;
    this.ambient = ambient;
  }

  public ImageElement(Uri uri) {
    this.uri = uri;
  }

  public Uri getUri() {
    return uri;
  }

  public void setUri(Uri uri) {
    this.uri = uri;
  }

  public String getTripName() {
    return tripName;
  }

  public void setTripName(String tripName) {
    this.tripName = tripName;
  }

  public float getLat() {
    return lat;
  }

  public void setLat(float lat) {
    this.lat = lat;
  }

  public float getLng() {
    return lng;
  }

  public void setLng(float lng) {
    this.lng = lng;
  }

  public float getBarometer() {
    return barometer;
  }

  public void setBarometer(float barometer) {
    this.barometer = barometer;
  }

  public float getAmbient() {
    return ambient;
  }

  public void setAmbient(float ambient) {
    this.ambient = ambient;
  }

  @BindingAdapter({ "avatar" })
  public static void loadImage(ImageView imageView, Uri uri) {
    final String imageKey = uri.toString();
    final Context context = imageView.getContext();
    Bitmap bitmap = ((MainActivity) context).getBitmapFromMemCache(imageKey);
    if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
      } else {
        if (BitmapWorkerTask.cancelPotentialWork(uri, imageView)) {
          BitmapWorkerTask task = new BitmapWorkerTask(context, imageView);
          Bitmap placeholder = BitmapFactory
                  .decodeResource(context.getResources(), R.drawable.placeholder);
          final AsyncDrawable asyncDrawable =
              new AsyncDrawable(context.getResources(), placeholder, task);
          imageView.setImageDrawable(asyncDrawable);
          task.execute(uri);
        }
      }
//  if (items.get(position) != null) {
//      final String imageKey = items.get(position).toString();
//      Bitmap bitmap = ((MainActivity) context).getBitmapFromMemCache(imageKey);
//      if (bitmap != null) {
//
//      } else {
//        ImageElement imageElement = items.get(position);
//        if (BitmapWorkerTask.cancelPotentialWork(uri, holder.imageView)) {
//          BitmapWorkerTask task = new BitmapWorkerTask(context, holder.imageView);
//          final AsyncDrawable asyncDrawable =
//              new AsyncDrawable(context.getResources(), placeholder, task);
//          holder.imageView.setImageDrawable(asyncDrawable);
//          task.execute(uri);
//        }
//      }
//    }
  }
}
