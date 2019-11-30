package com.photour.ui.visitnew;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.photour.database.PhotoRepository;
import com.photour.database.VisitRepository;
import com.photour.model.Photo;
import com.photour.model.Visit;
import java.util.Date;

/**
 * A ViewModel for {@link NewVisitFragment} and {@link StartVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class NewVisitViewModel extends AndroidViewModel {

  private VisitRepository visitRepository;
  private PhotoRepository photoRepository;

  private long visitRowId = 0;
  private Date newVisitDate = new Date();

  private MutableLiveData<String> newVisitTitle = new MutableLiveData<>();
  private Long elapsedTime;

  /**
   * Constructor of {@link NewVisitViewModel}
   *
   * @param application Application of {@link com.photour.MainActivity}
   */
  public NewVisitViewModel(@NonNull Application application) {
    super(application);
    visitRepository = new VisitRepository(application);
    photoRepository = new PhotoRepository(application);
  }

  /**
   * Insert the current new visit into the database
   */
  public void insertVisit() {
    if (visitRowId == 0) {
      visitRowId = visitRepository
          .insert(Visit.create(0, newVisitTitle.getValue(), new Date(), null));
    }
  }

  /**
   * End the current new visit
   *
   * @param startVisitMap A {@link StartVisitMap} instance
   */
  void endVisit(StartVisitMap startVisitMap) {
    if (visitRowId != 0) {
      visitRepository.update(visitRowId, startVisitMap.latLngList);
    }
  }

  void insertPhoto(Photo photo) {
    if (visitRowId != 0) {
      photoRepository.insert(photo);
    }
  }

  /**
   * Get the row ID of the current visit
   *
   * @return long The row ID of the current visit
   */
  public long getVisitRowId() {
    return visitRowId;
  }

  /**
   * Set the row ID of the current visit
   *
   * @param visitRowId The new value of the row ID
   */
  void setVisitRowId(long visitRowId) {
    this.visitRowId = visitRowId;
  }

  /**
   * Get the title of the new visit
   *
   * @return LiveData<String> The title of the new visit
   */
  public LiveData<String> getNewVisitTitle() {
    return newVisitTitle;
  }

  /**
   * Set the title of the new visit
   *
   * @param newVisitTitle The new title of the new visit
   */
  void setNewVisitTitle(String newVisitTitle) {
    this.newVisitTitle.setValue(newVisitTitle);
  }

  /**
   * Get the value of elapsedTime
   *
   * @return Long The value of elapsedTime
   */
  Long getElapsedTime() {
    return elapsedTime;
  }

  /**
   * Set the value of elapsedTime
   *
   * @param elapsedTime The new value of elapsedTime
   */
  void setElapsedTime(final long elapsedTime) {
    this.elapsedTime = elapsedTime;
  }
}
