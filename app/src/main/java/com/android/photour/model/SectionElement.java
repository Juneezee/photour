package com.android.photour.model;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

public class SectionElement {

  @PrimaryKey
  public int uid;
  @ColumnInfo(name = "title")
  private String title;
  @ColumnInfo(name = "image_element")
  private List<ImageElement> imageElements;

  public SectionElement(String title) {
    this.title = title;
    this.imageElements = new ArrayList<>();
  }

  public SectionElement(String title, ImageElement imageElement) {
    this.title = title;
    this.imageElements = new ArrayList<>();
    this.imageElements.add(imageElement);
  }

  public SectionElement(String title, List<ImageElement> imageElementList) {
    this.title = title;
    this.imageElements = imageElementList;
  }

  public void addImageElement(ImageElement imageElement) {
    this.imageElements.add(imageElement);
  }

  public List<ImageElement> getImageElements() {
    return imageElements;
  }

  public String getTitle() {
    return title;
  }

  public String toString() {
    return title + ": " + imageElements.toString();
  }


}
