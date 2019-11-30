package com.photour.ui.visitnew;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.photour.database.PhotoRepository;
import com.photour.database.VisitRepository;
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

  // Boolean to check if the current visit has been inserted into the database
  private boolean isVisitInserted = false;

  long visitRowId;

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

  public void insertVisit() {
    if (!isVisitInserted) {
      visitRowId = visitRepository
          .insert(Visit.create(0, newVisitTitle.getValue(), new Date(), null));
      isVisitInserted = true;
    }
  }

  /**
   * Check if the current visit has been inserted into the database
   *
   * @return boolean {@code true} if current visit has been inserted into the database
   */
  boolean isVisitInserted() {
    return isVisitInserted;
  }

  /**
   * Set the value of <var>isVisitInserted</var>
   *
   * @param value The new value of <var>isVisitInserted</var>
   */
  void setIsVisitInserted(boolean value) {
    isVisitInserted = value;
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
