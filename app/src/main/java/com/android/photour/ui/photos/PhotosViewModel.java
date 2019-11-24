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

import com.android.photour.database.ImageRepository;
import com.android.photour.model.ImageElement;
import com.android.photour.model.SectionElement;
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

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();

  // Statics for readwrite images
  private ImageRepository imageRepository;
//  private MutableLiveData<List<ImageElement>> _images = new MutableLiveData<>();
  public LiveData<List<ImageElement>> images;

  //  static final List<SectionElement> ITEMS = new ArrayList<>();
  private ContentObserver contentObserver = null;

  /**
   * Constructor for PhotosViewModel
   *
   * @param application Application of MainActivity
   */
  public PhotosViewModel(@NonNull Application application) {
    super(application);
    imageRepository = new ImageRepository(application);
    loadImages();
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
   images = imageRepository.getAllImages();
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
   * Helper class to get name of folder from whole path
   *
   * @param dir Full directory path
   * @return String folder name where the file sits
   */
  private String getPath(String dir) {
    String[] temp = dir.split("/");
    return temp[temp.length - 2];
  }


}
