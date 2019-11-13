package com.android.photour;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageElement {
    private int image = -1;
    private File file = null;

  private String title;
  private List<File> file;

  public ImageElement(String title) {
    this.title = title;
  }


  public ImageElement(String title, File file) {
    this.title = title;
    this.file = new ArrayList<>();
    this.file.add(file);
  }

  public ImageElement(String title, List<File> fileList) {
    this.title = title;
    this.file = fileList;
  }
  public void addFile (File file) {
    this.file.add(file);
  }

  public List<File> getFiles() {
    return file;
  }
}
