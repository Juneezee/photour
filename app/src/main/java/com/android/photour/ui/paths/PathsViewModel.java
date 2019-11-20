package com.android.photour.ui.paths;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PathsViewModel extends ViewModel {

  private MutableLiveData<String> mText;

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();

  public PathsViewModel() {
    mText = new MutableLiveData<>();
    mText.setValue("This is paths fragment");
  }

  public LiveData<String> getText() {
    return mText;
  }

  /**
   * Get the placeholder text to display when no photos are available
   *
   * @return The placeholder text
   */
  public LiveData<String> getPlaceholderText() {
    return placeholderText;
  }

  /**
   * Set the placeholder text as "No photos yet"
   *
   * @param isEmpty True to set the placeholder text as empty
   */
  void setPlaceholderText(boolean isEmpty) {
    placeholderText.setValue(
        isEmpty ? "" : "No paths yet " + new String(Character.toChars(0x1F60A)));
  }
}
