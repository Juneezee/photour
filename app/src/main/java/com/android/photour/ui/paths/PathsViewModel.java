package com.android.photour.ui.paths;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PathsViewModel extends ViewModel {

  private MutableLiveData<String> mText;

  public PathsViewModel() {
    mText = new MutableLiveData<>();
    mText.setValue("This is paths fragment");
  }

  public LiveData<String> getText() {
    return mText;
  }
}