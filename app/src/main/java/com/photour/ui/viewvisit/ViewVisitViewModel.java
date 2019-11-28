package com.photour.ui.viewvisit;

import android.app.Application;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.photour.database.ImageRepository;
import com.photour.model.ImageElement;
import com.photour.model.Visit;

import java.util.List;

public class ViewVisitViewModel extends AndroidViewModel {

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();
  private MutableLiveData<String[]> detailsArray = new MutableLiveData<>();
//  public MutableLiveData<Integer> currentImagePos = new MutableLiveData<>();
  private ImageRepository imageRepository;
  public Visit visit;
  public LiveData<List<ImageElement>> images;

  private ContentObserver contentObserver = null;

  public ViewVisitViewModel(@NonNull Application application) {
    super(application);
    imageRepository = new ImageRepository(application);

  }

  public MutableLiveData<String[]> getDetailsArray() {
    return detailsArray;
  }

  public void setDetailsArray(int currentImagePos) {
    String[] tempArray = {visit.visitTitle()};
    detailsArray.setValue(tempArray);
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
   * Set the placeholder text as "No photos for this visit"
   *
   * @param isEmpty True to set the placeholder text as empty
   */
  void setPlaceholderText(boolean isEmpty) {
    placeholderText.setValue(
            isEmpty ? "" : "No images for this visit " + new String(Character.toChars(0x1F60A)));
  }

  public void loadImages() {
    images = imageRepository.getImagesforVisit(visit.visitTitle());
    setDetailsArray(0);
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
}
