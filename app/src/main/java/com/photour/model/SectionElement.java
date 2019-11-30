package com.photour.model;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for SectionElement
 */
public class SectionElement {

  private String title;
  private List<Photo> photos;

  /**
   * Constructor for Section Element
   * @param title title of section
   */
  public SectionElement(String title) {
    this.title = title;
    this.photos = new ArrayList<>();
  }

  /**
   * Constructor for Section Element
   *
   * @param title title of section
   * @param photo A list of photos in the section
   */
  public SectionElement(String title, Photo photo) {
    this.title = title;
    this.photos = new ArrayList<>();
    this.photos.add(photo);
  }

  /**
   * Constructor for Section Element
   *
   * @param title Title of section
   * @param photoList A list of photos in the section
   */
  public SectionElement(String title, List<Photo> photoList) {
    this.title = title;
    this.photos = photoList;
  }

  /**
   * Add Photo to the section
   *
   * @param photo Photo to be inserted
   */
  public void addPhoto(Photo photo) {
    this.photos.add(photo);
  }

  /**
   * Accessor to Photos in section
   *
   * @return List of Photo
   */
  public List<Photo> getPhotos() {
    return photos;
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
  @NonNull
  public String toString() {
    return title + ": " + photos.toString();
  }
}
