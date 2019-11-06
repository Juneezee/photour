package com.android.photour.ui.visit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.text.DateFormat;
import java.util.Calendar;

public class VisitViewModel extends ViewModel {

  private MutableLiveData<String> textTitle, textDate;

  public VisitViewModel() {
    textTitle = new MutableLiveData<>();
    textTitle.setValue("Start a New Visit");

    Calendar calendar = Calendar.getInstance();
    String today = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

    textDate = new MutableLiveData<>();
    textDate.setValue(today);
  }

  public LiveData<String> getTextTitle() {
    return textTitle;
  }

  public LiveData<String> getTextDate() {
    return textDate;
  }

}
