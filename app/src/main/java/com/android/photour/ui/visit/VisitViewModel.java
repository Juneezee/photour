package com.android.photour.ui.visit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * A ViewModel for {@link VisitFragment} and {@link StartVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitViewModel extends ViewModel {

  private MutableLiveData<String> newVisitTitle = new MutableLiveData<>();

  private Long elapsedTime;

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
    this.newVisitTitle.setValue(newVisitTitle.isEmpty() ? "Untitled trip" : newVisitTitle);
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
