package com.android.photour.model;

public class TripElement {

  private String tripname;
  private int photoNo;
  private String displayPhoto;

  public TripElement(String tripname, int photoNo, String displayPhoto) {
    this.tripname = tripname;
    this.photoNo = photoNo;
    this.displayPhoto = displayPhoto;
  }

  public String getTripname() {
    return tripname;
  }

  public void setTripname(String tripname) {
    this.tripname = tripname;
  }

  public int getPhotoNo() {
    return photoNo;
  }

  public void setPhotoNo(int photoNo) {
    this.photoNo = photoNo;
  }

  public String getDisplayPhoto() {
    return displayPhoto;
  }

  public void setDisplayPhoto(String displayPhoto) {
    this.displayPhoto = displayPhoto;
  }
}
