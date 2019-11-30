package com.photour.ui.visit;

import android.app.Application;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.libraries.maps.model.LatLng;
import com.photour.database.PhotoRepository;
import com.photour.helper.PreferenceHelper;
import com.photour.model.Photo;
import com.photour.model.Visit;
import java.util.List;

/**
 * A ViewModel for {@link VisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitViewModel extends AndroidViewModel {

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();
  private MutableLiveData<String[]> detailsArray = new MutableLiveData<>();
  private PhotoRepository photoRepository;
  public Visit visit;
  public LiveData<List<Photo>> images;
  private boolean hasSensorsReading;

  private ContentObserver contentObserver = null;

  /**
   * Constructor for VisitViewModel
   *
   * @param application Application of MainActivity
   */
  public VisitViewModel(@NonNull Application application) {
    super(application);
    photoRepository = new PhotoRepository(application);
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
   * @param currentImagePos position of Photo in images list
   * @return LatLng of Photo
   */
  public LatLng setDetails(int currentImagePos) {

    Photo photo = images.getValue().get(currentImagePos);
    hasSensorsReading = photo.hasSensorsReading();
    String unit = PreferenceHelper.tempUnit(getApplication());
    String[] tempArray = {
            visit.visitTitle(),
            photo.getDateInString(),
            String.valueOf(unit.equals("c") ? (photo.temperatureCelsius()):(photo.temperatureFahrenheit())).concat(unit),
            String.valueOf(photo.pressure()),
            photo.filePath(),
            String.valueOf(photo.hasSensorsReading())};
    detailsArray.setValue(tempArray);

    return new LatLng(photo.latitude(), photo.longitude());
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
    images = photoRepository.getAllPhotosInVisit(visit.id());
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
