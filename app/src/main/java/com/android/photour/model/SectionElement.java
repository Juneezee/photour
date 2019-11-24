package com.android.photour.model;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for SectionElement
 */
public class SectionElement {

  private String title;
  private List<ImageElement> imageElements;

  /**
   * Constructor for Section Element
   * @param title title of section
   */
  public SectionElement(String title) {
    this.title = title;
    this.imageElements = new ArrayList<>();
  }

  /**
   * Constructor for Section Element
   *
   * @param title title of section
   * @param imageElement Image Element in the section
   */
  public SectionElement(String title, ImageElement imageElement) {
    this.title = title;
    this.imageElements = new ArrayList<>();
    this.imageElements.add(imageElement);
  }

  /**
   * Constructor for Section Element
   *
   * @param title title of section
   * @param imageElementList Image Elementx in the section
   */
  public SectionElement(String title, List<ImageElement> imageElementList) {
    this.title = title;
    this.imageElements = imageElementList;
  }

  /**
   * Add Image Element to the section
   *
   * @param imageElement ImageElement to be inserted
   */
  public void addImageElement(ImageElement imageElement) {
    this.imageElements.add(imageElement);
  }

  /**
   * Accessor to ImageElements in section
   *
   * @return List of ImageElement
   */
  public List<ImageElement> getImageElements() {
    return imageElements;
  }

  /**
   * Getter for Title
   *
   * @return title of section
   */
  public String getTitle() {
    return title;
  }

  /**
   * Return string for class
   *
   * @return string
   */
  public String toString() {
    return title + ": " + imageElements.toString();
  }
}
