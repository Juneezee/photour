package com.android.photour;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class ImageElement {

  private String title;
  private List<Uri> uri;

  public ImageElement(String title) {
    this.title = title;
    this.uri = new ArrayList<>();
  }


  public ImageElement(String title, Uri uri) {
    this.title = title;
    this.uri = new ArrayList<>();
    this.uri.add(uri);
  }

  public ImageElement(String title, List<Uri> uriList) {
    this.title = title;
    this.uri = uriList;
  }

  public void addUri(Uri uri) {
    this.uri.add(uri);
  }

  public List<Uri> getUris() {
    return uri;
  }

  public String getTitle() {
    return title;
  }

  public String toString() {
    return title + ": " + uri.toString();
  }
}
