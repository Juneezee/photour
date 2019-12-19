package com.photour.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.google.android.libraries.maps.model.LatLng;
import com.photour.helper.FutureHelper;
import com.photour.model.Visit;
import com.photour.ui.photo.PhotoFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Visits database
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitRepository {

  private VisitDao visitDao;

  /**
   * Constructor of {@link VisitRepository}
   *
   * @param application Application of MainActivity
   */
  public VisitRepository(Application application) {
    AppDatabase db = AppDatabase.getDatabase(application);
    visitDao = db.visitDao();
  }

  /**
   * Insert a new visit into Visits table
   *
   * @param visit The new visit to be inserted
   * @return long The row ID of the new inserted visit
   */
  public long insert(Visit visit) {
    Object result = FutureHelper.genericFuture(() -> visitDao.insert(visit));

    return result == null ? 0 : (long) result;
  }

  /**
   * Update the elapsedTime and latLngList of a visit
   *
   * @param id The row ID of the visit
   * @param elapsedTime Elapsed time of the visit
   * @param latLngList The new list of LatLng
   */
  public void update(final long id, final long elapsedTime, final ArrayList<LatLng> latLngList) {
    AppDatabase.databaseExecutor.execute(() -> visitDao.update(id, elapsedTime, latLngList));
  }

  /**
   * Update the title of a visit
   *
   * @param id The row ID of the visit
   * @param visitTitle The new visit title of the visit
   */
  public boolean update(long id, String visitTitle) {
    return FutureHelper.rowOperationFuture(() -> visitDao.update(id, visitTitle));
  }

  /**
   * Delete a visit from the Visits table
   *
   * @param visit The {@link Visit} to be deleted
   * @return int The row ID of the deleted visit
   */
  public boolean delete(Visit visit) {
    return FutureHelper.rowOperationFuture(() -> visitDao.delete(visit));
  }

  /**
   * Gets all visits in database
   *
   * @return LiveData<List < Visit>> List of visits
   */
  public LiveData<List<Visit>> getAllVisits() {
    return visitDao.getAllVisits();
  }

  /**
   * Function to get visit title for {@link PhotoFragment}
   *
   * @param id ID of Visit
   * @return String title of Visit
   */
  public String getVisitTitle(final long id) {
    return FutureHelper.stringFuture(() -> visitDao.getVisitTitle(id));
  }

  /**
   * Get LiveData type of {@link Visit} from the Visits table
   *
   * @param id the ID of the visit
   * @return LiveData<Visit> the visit of LiveData type
   */
  public LiveData<Visit> getLiveVisit(final long id) {
    return FutureHelper.genericFuture(() -> visitDao.getLiveVisit(id));
  }
}
