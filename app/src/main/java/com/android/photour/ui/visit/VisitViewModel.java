package com.android.photour.ui.visit;

import androidx.lifecycle.ViewModel;

/**
 * A ViewModel for {@link VisitFragment} and {@link StartVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitViewModel extends ViewModel {

  private Long elapsedTime;

  /**
   * Get the value of elapsedTime
   *
   * @return The value of elapsedTime
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
