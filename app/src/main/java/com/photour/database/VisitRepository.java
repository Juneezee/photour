package com.photour.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.photour.model.Visit;
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

    Future<Long> future = AppDatabase.databaseWriteExecutor.submit(insertCallable);

    try {
      rowId = future.get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    return rowId;
  }

  /**
   * Gets all visits in database
   *
   * @return List of visits
   */
  public LiveData<List<Visit>> getAllVisits() {
    return visitDao.getAllVisits();
  }
}
