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

/**
 * A ViewModel for {@link PhotosFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosViewModel extends AndroidViewModel {

  static final int QUERY_BY_DATE = 0;
  static final int QUERY_BY_PATH = 1;
  private int sortMode;

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();

  // Statics for readwrite images
  private MutableLiveData<List<ImageElement>> _images = new MutableLiveData<>();
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
  }

  /**
   * Get the placeholder text to display when no photos are available
   *
   * @return LiveData<String> The placeholder text
   */
  public LiveData<String> getPlaceholderText() {
    return placeholderText;
  }

  /**
   * Set the placeholder text as "No photos yet"
   *
   * @param isEmpty True to set the placeholder text as empty
   */
  void setPlaceholderText(boolean isEmpty) {
    placeholderText.setValue(
        isEmpty ? "" : "No photos yet " + new String(Character.toChars(0x1F60A)));
  }

  /**
   * Helper function to recalculate number of columns
   *
   * @param context Context of application
   * @param columnWidthDp Size of column in dp
   * @return int number of columns
   */
  static int calculateNoOfColumns(Context context, float columnWidthDp) {
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
   * @return List lists of ImageElement, each representing a section in the gallery
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
    String[] selectionArgs = new String[]{"%DCIM%"};

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
    if (imageElement != null) {
      images.add(imageElement);
    }
    query.close();

    return images;
  }

  /**
   * Helper class to get name of folder from whole path
   *
   * @param dir Full directory path
   * @return String folder name where the file sits
   */
  private String getPath(String dir) {
    String[] temp = dir.split("/");
    return temp[temp.length - 2];
  }

  /**
   * Switch sorting mode and call loadImages() to reset data set
   *
   * @param type The type to sort the photos (by date or by path)
   */
  void switchSortMode(int type) {
    if (sortMode != type) {
      sortMode = type;
      loadImages();
    }
  }

}
