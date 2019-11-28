package com.photour.ui.viewvisit;

import android.app.Application;
import android.database.ContentObserver;
import android.media.Image;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.maps.model.LatLng;
import com.photour.database.ImageRepository;
import com.photour.model.ImageElement;
import com.photour.model.Visit;

import java.util.List;

/**
 * A ViewModel for {@link ViewVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ViewVisitViewModel extends AndroidViewModel {

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();
  private MutableLiveData<String[]> detailsArray = new MutableLiveData<>();
  private ImageRepository imageRepository;
  public Visit visit;
  public LiveData<List<ImageElement>> images;
  private boolean hasSensorsReading = false;

  private ContentObserver contentObserver = null;

  /**
   * Constructor for ViewVisitViewModel
   *
   * @param application Application of MainActivity
   */
  public ViewVisitViewModel(@NonNull Application application) {
    super(application);
    imageRepository = new ImageRepository(application);
  }

  /**
   * Getter for detailsArray
   *
   * @return String[] detailsArray
   */
  public MutableLiveData<String[]> getDetailsArray() {
    return detailsArray;
  }

  /**
   * Setter for detailsArray
   *
   * @param currentImagePos position of ImageElement in images list
   * @return LatLng of ImageElement
   */
  public LatLng setDetails(int currentImagePos) {

    ImageElement imageElement = images.getValue().get(currentImagePos);
    hasSensorsReading = imageElement.hasSensorsReading();
    String[] tempArray = {
            visit.visitTitle(),
            imageElement.getDateInString(),
            String.valueOf(imageElement.temperature()),
            String.valueOf(imageElement.pressure()),
            imageElement.filePath()};
    detailsArray.setValue(tempArray);

    return new LatLng(imageElement.latitude(),imageElement.longitude());
  }

  /**
   * Returns if the ImageElement has sensorReading
   *
   * @return boolean True if the ImageElement has sensor reading, else False.
   */
  public boolean hasSensorsReading() {
    return hasSensorsReading;
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
   * Set the placeholder text as "No images for this visit"
   *
   * @param isEmpty True to set the placeholder text as empty
   */
  void setPlaceholderText(boolean isEmpty) {
    placeholderText.setValue(
            isEmpty ? "" : "No images for this visit " + new String(Character.toChars(0x1F60A)));
  }

  /**
   * Helper function to setup images LiveData with the Room
   */
  public void loadImages() {
    images = imageRepository.getImagesforVisit(visit.visitTitle());
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
