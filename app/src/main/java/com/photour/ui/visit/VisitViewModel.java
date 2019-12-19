package com.photour.ui.visit;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.photour.database.PhotoRepository;
import com.photour.database.VisitRepository;
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

  public Visit visit;
  LiveData<Visit> liveVisit;

  public LiveData<List<Photo>> photos;
  private PhotoRepository photoRepository;
  private VisitRepository visitRepository;

  /**
   * Constructor for VisitViewModel
   *
   * @param application Application of MainActivity
   */
  public VisitViewModel(@NonNull Application application) {
    super(application);
    photoRepository = new PhotoRepository(application);
    visitRepository = new VisitRepository(application);
  }

  /**
   * Set the value of non-LiveData visit and liveData visit
   *
   * @param visit The visit that is passed from {@link com.photour.ui.visits.VisitsFragment}
   */
  void initVisit(Visit visit) {
    this.visit = visit;
    liveVisit = visitRepository.getLiveVisit(visit.id());

    loadImages();
  }

  /**
   * Get the visit
   *
   * @return LiveData<Visit> The visit
   */
  public LiveData<Visit> getVisit() {
    return liveVisit;
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
   * @param currentImagePos position of Photo in photos list
   * @return LatLng of Photo
   */
  int setDetails(int currentImagePos) {
    if (currentImagePos < 0 || photos.getValue() == null) {
      return -1;
    }

    Photo photo = photos.getValue().get(currentImagePos);
    String unit = PreferenceHelper.tempUnit(getApplication());
    String[] tempArray = {
        photo.getDateInString(),
        String.valueOf(unit.equals("c") ? (photo.temperatureCelsius())
            : (photo.temperatureFahrenheit())).concat(unit),
        String.valueOf(photo.pressure()),
        photo.filePath(),
        String.valueOf(photo.hasSensorsReading())
    };

    detailsArray.setValue(tempArray);

    return photo.id();
  }

  /**
   * Update the title of the visit in {@link EditVisitFragment}
   *
   * @param visitTitle The new visit for the visit
   * @return boolean {@code true} if the operation is successful, {@code false} otherwise
   */
  boolean updateVisitTitle(String visitTitle) {
    return visitRepository.update(visit.id(), visitTitle.isEmpty() ? "Untitled visit" : visitTitle);
  }

  /**
   * Delete a visit
   *
   * @return boolean {@code true} if the visit has been deleted successfully, otherwise {@code
   * false}
   */
  boolean deleteVisit() {
    return visitRepository.delete(visit);
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
        isEmpty ? "" : "No photos for this visit " + new String(Character.toChars(0x1F60A)));
  }

  /**
   * Helper function to setup photos LiveData with the Room
   */
  private void loadImages() {
    photos = photoRepository.getAllPhotosInVisit(visit.id());
  }

}
