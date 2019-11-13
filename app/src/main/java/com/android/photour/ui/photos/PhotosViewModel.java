package com.android.photour.ui.photos;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import com.android.photour.ImageElement;
import com.android.photour.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotosViewModel extends ViewModel {

  // Statics for readwrite images
  static final List<ImageElement> ITEMS = new ArrayList<>();
  private ImageElement img;

  public int getPermission

  /**
   * Helper function to recalculate number of columns
   *
   * @param context Context of application
   * @param columnWidthDp Size of column in dp
   * @return int number of columns
   */
  int calculateNoOfColumns(Context context, float columnWidthDp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
    return (int) (screenWidthDp / columnWidthDp + 0.5);
  }

  /**
   *
   * @param dir
   * @param config 0 = By trips(folder), 1 = By Date
   */
  public static void loadSavedImages(File dir, int config) {
    ITEMS.clear();
    //by trips
    if (config == 0) {
      File[] files = dir.listFiles();
      List<File> tempFiles = new ArrayList<>();
      for (File file : files) {

      }
    } else if (config == 1) {

    }
    addItem(new ImageElement(dir.toString()));
  }

  private List<File> createImageElement(File dir, List<File> tempFiles) {
    ImageElement imageElement = new ImageElement(dir.toString());
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (File file : files) {
        String absolutePath = file.getAbsolutePath();
        String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
        if (extension.equals(".jpg") || extension.equals(".png")) {
//          loadImage(file);
        }
      }
    }
  }

  public static void loadImage(File file) {
    PictureItem newItem = new PictureItem();
    newItem.uri = Uri.fromFile(file);
    newItem.date = getDateFromUri(newItem.uri);
    addItem(newItem);
  }

  private static void addItem(ImageElement item) {
    ITEMS.add(0, item);
  }
}
