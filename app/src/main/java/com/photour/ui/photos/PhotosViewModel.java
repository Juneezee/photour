package com.photour.ui.photos;

import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.photour.R;
import com.photour.database.PhotoRepository;
import com.photour.model.Photo;
import java.util.List;

/**
 * A ViewModel for {@link PhotosFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosViewModel extends AndroidViewModel {

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();
  public int sortMode;

  // Statics for readwrite photos
  private PhotoRepository photoRepository;
  public LiveData<List<Photo>> photos;

  private ContentObserver contentObserver = null;

  /**
   * Constructor for PhotosViewModel
   *
   * @param application Application of MainActivity
   */
  public PhotosViewModel(@NonNull Application application) {
    super(application);
    photoRepository = new PhotoRepository(application);
    sortMode = R.id.by_date_desc;
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
   * Calls {@link PhotoRepository#getAllLivePhotosDesc()} to get all photos from database. Sets up an
   * Observer to observe the viewmodel and calls this method if there is any change.
   */
  void loadPhotos() {
    photos = photoRepository.getAllLivePhotosDesc();
    if (contentObserver == null) {
      contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
          super.onChange(selfChange);
          loadPhotos();
        }
      };
      this.getApplication().getContentResolver().registerContentObserver(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
    }
  }

  /**
   * Calls {@link PhotoRepository#getAllPhotos()} to get all photos from database.
   *
   * @return List<Photo> A list of photos
   */
  List<Photo> loadPhotosForClusterMarker() {
    return photoRepository.getAllPhotos();
  }
}
