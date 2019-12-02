package com.photour.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.google.android.libraries.maps.model.LatLng;
import com.photour.model.Visit;
import com.photour.ui.photo.PhotoFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    Callable<Long> insertCallable = () -> visitDao.insert(visit);
    long rowId = 0;

    Future<Long> future = AppDatabase.databaseExecutor.submit(insertCallable);

    try {
      rowId = future.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    return rowId;
  }

  /**
   * Update the latLngList of a visit
   *
   * @param id The row ID of the visit
   * @param latLngList The new list of LatLng
   */
  public void update(final long id, final long elapsedTime, final ArrayList<LatLng> latLngList) {
    AppDatabase.databaseExecutor.execute(() -> visitDao.update(id, elapsedTime, latLngList));
  }

  /**
   * Gets all visits in database
   *
   * @return LiveData<List<Visit>> List of visits
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
    Callable<String> titleCallable = () -> visitDao.getVisitTitle(id);

    Future<String> future = AppDatabase.databaseExecutor.submit(titleCallable);

    try {
      return future.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    return "";
  }
}
