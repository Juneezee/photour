package com.android.photour.ui.photos;

import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.photour.ImageElement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotosViewModel extends AndroidViewModel {

  private static final int QUERY_BY_DATE = 0;
  private static final int QUERY_BY_TRIPS = 1;
  public int sortMode;
  // Statics for readwrite images
  private MutableLiveData<List<ImageElement>> _images = new MutableLiveData<List<ImageElement>>();
  public LiveData<List<ImageElement>> images = _images;
  //  static final List<ImageElement> ITEMS = new ArrayList<>();
  private ContentObserver contentObserver = null;

  /**
   * Constructor for PhotosViewModel
   *
   * @param application Application of MainActivity
   */
  public PhotosViewModel(@NonNull Application application) {
    super(application);
    sortMode = QUERY_BY_DATE;
    loadImages();
  }

  /**
   * Helper function to recalculate number of columns
   *
   * @param context Context of application
   * @param columnWidthDp Size of column in dp
   * @return int number of columns
   */
  public static int calculateNoOfColumns(Context context, float columnWidthDp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
    return (int) (screenWidthDp / columnWidthDp + 0.5);
  }

  /**
   * Calls queryImages() to get all images from external storage. Sets up an Observer to observe the
   * viewmodel and calls this method if there is any change.
   */
  public void loadImages() {
    List<ImageElement> imageList = queryImages();
    _images.postValue(imageList);
    if (contentObserver == null) {
      contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
          super.onChange(selfChange);
          loadImages();
        }
      };
      this.getApplication().getContentResolver().registerContentObserver(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
    }
  }

  /**
   * Uses Mediastore query to get all images from a given folder according to the sort
   * configuration.
   *
   * @return lists of ImageElement, each representing a section in the gallery
   */
  private List<ImageElement> queryImages() {
    List<ImageElement> images = new ArrayList<>();

    //Columns to retrieve with query
    String[] projection = new String[]{MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
        "_data"
    };

    String selection = "( _data LIKE ? )";
    String[] selectionArgs = new String[]{"%Pictures%"};

    String sortOrder = sortMode == QUERY_BY_DATE ?
        MediaStore.Images.Media.DATE_TAKEN + " DESC" : "_data DESC";

    Cursor query = getApplication().getContentResolver().query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    );
    int i = 0;
    String previousTitle = "";
    ImageElement imageElement = null;

    //Iterates through query and append them into ImageElement
    while (i < query.getCount()) {
      query.moveToPosition(i);
      String currentTitle;
      if (sortMode == QUERY_BY_DATE) {
        Date date = new Date(query.getLong(query.getColumnIndexOrThrow("datetaken")));
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        currentTitle = sdf.format(date);
      } else {
        currentTitle = getPath(query.getString(query.getColumnIndexOrThrow("_data")));
      }
      long columnIndex = query.getLong(query.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
      Uri contentUri = ContentUris.withAppendedId(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columnIndex);
      if (!previousTitle.equals(currentTitle)) {
        if (imageElement != null) {
          images.add(imageElement);
        }
        imageElement = new ImageElement(currentTitle);
        previousTitle = currentTitle;
      }
      imageElement.addUri(contentUri);
      i++;
    }
    images.add(imageElement);
    query.close();

    return images;
  }

  /**
   * Helper class to get name of folder from whole path
   *
   * @param dir Full directory path
   * @return folder name where the file sits
   */
  private String getPath(String dir) {
    String[] temp = dir.split("/");
    return temp[temp.length - 2];
  }

  /**
   * alternates sortMode and calls loadImages() to reset data set
   */
  public void switchSortMode() {
    sortMode = sortMode == QUERY_BY_TRIPS ? QUERY_BY_DATE : QUERY_BY_TRIPS;
    loadImages();
  }
}
