package com.android.photour.ui.visit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * A ViewModel for {@link VisitFragment} and {@link StartVisitFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitViewModel extends ViewModel {

  private MutableLiveData<String> textDate;
  private Long elapsedTime;

  /**
   * Constructor of {@link VisitViewModel}
   */
  public VisitViewModel() {
    Calendar calendar = Calendar.getInstance();
    String today = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

    textDate = new MutableLiveData<>();
    textDate.setValue(today);
  }

  /**
   * Get the value of textDate
   *
   * @return LiveData The value of textDate
   */
  LiveData<String> getTextDate() {
    return textDate;
  }

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
