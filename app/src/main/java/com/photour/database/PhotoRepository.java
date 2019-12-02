package com.photour.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.photour.model.Photo;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository for Photos database
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotoRepository {

  private PhotoDao photoDao;

  /**
   * Constructor of {@link PhotoRepository}
   *
   * @param application Application of MainActivity
   */
  public PhotoRepository(Application application) {
    AppDatabase db = AppDatabase.getDatabase(application);
    photoDao = db.imageDao();
  }

  /**
   * Add Photo into the database
   *
   * @param photo Photo object to be inserted into the database
   */
  public void insert(Photo photo) {
    AppDatabase.databaseExecutor.execute(() -> photoDao.insert(photo));
  }

  /**
   * Get all Photos from database in LiveData form
   *
   * @return LiveData<List<Photo>> List of Photos
   */
  public LiveData<List<Photo>> getAllLivePhotosDesc() {
    Callable<LiveData<List<Photo>>> getCallable = () -> photoDao.getAllDesc();

    Future<LiveData<List<Photo>>> future = AppDatabase.databaseExecutor.submit(getCallable);

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get all Photos from database
   *
   * @return List<Photo>
   */
  public List<Photo> getAllPhotos() {
    Callable<List<Photo>> getCallable = () -> photoDao.getAllPhotos();

    Future<List<Photo>> future = AppDatabase.databaseExecutor.submit(getCallable);

    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Gets all Images that is in the visit in database
   *
   * @param visitId Primary key ID of the visit
   * @return List of Photo
   */
  public LiveData<List<Photo>> getAllPhotosInVisit(int visitId) {
    return photoDao.findByVisit(visitId);
  }
}
