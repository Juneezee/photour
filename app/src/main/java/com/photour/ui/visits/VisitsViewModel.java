package com.photour.ui.visits;

import android.app.Application;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.photour.R;
import com.photour.database.VisitRepository;
import com.photour.model.Visit;
import java.util.List;

/**
 * A ViewModel for {@link VisitsFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitsViewModel extends AndroidViewModel {

  private MutableLiveData<String> placeholderText = new MutableLiveData<>();

  private VisitRepository visitRepository;
  public LiveData<List<Visit>> visits;

  private ContentObserver contentObserver = null;

  /**
   * Constructor for VisitsViewModel
   *
   * @param application Application of MainActivity
   */
  public VisitsViewModel(@NonNull Application application) {
    super(application);
    visitRepository = new VisitRepository(application);
    loadVisit();
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
   * Set the placeholder text as "No photos yet"
   *
   * @param isEmpty True to set the placeholder text as empty
   */
  void setPlaceholderText(boolean isEmpty) {
    placeholderText
        .setValue(isEmpty ? "" : "No visits yet " + new String(Character.toChars(0x1F60A)));
  }

  /**
   * Check if the current sorting mode is by oldest date first
   *
   * @return boolean {@code true} if the visits are to sort by oldest date first
   */
  boolean isSortByAsc(int sortMode) {
    return sortMode == R.id.visits_asc;
  }

  /**
   * Helper function to setup visits LiveData with the Room
   */
  private void loadVisit() {
    visits = visitRepository.getAllVisits();
    if (contentObserver == null) {
      contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
          super.onChange(selfChange);
          loadVisit();
        }
      };
      this.getApplication().getContentResolver().registerContentObserver(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
    }
  }
}
