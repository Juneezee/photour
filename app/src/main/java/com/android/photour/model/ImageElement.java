package com.android.photour.model;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
}
