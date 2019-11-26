package com.android.photour.ui.paths;

import android.app.Application;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.photour.R;
import com.android.photour.database.ImageRepository;
import com.android.photour.model.ImageElement;
import com.android.photour.model.TripElement;

import java.util.List;

public class PathsViewModel extends AndroidViewModel {


  private MutableLiveData<String> placeholderText = new MutableLiveData<>();
  public int sortMode;

  private ImageRepository imageRepository;
  public LiveData<List<TripElement>> trips;

  private ContentObserver contentObserver = null;

  public PathsViewModel(@NonNull Application application) {
    super(application);
    imageRepository = new ImageRepository(application);
    loadTrips();
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
        isEmpty ? "" : "No paths yet " + new String(Character.toChars(0x1F60A)));
  }

  public void loadTrips() {
    trips = imageRepository.getTrips();
    if (contentObserver == null) {
      contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
          super.onChange(selfChange);
          loadTrips();
        }
      };
      this.getApplication().getContentResolver().registerContentObserver(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
    }
  }
}
