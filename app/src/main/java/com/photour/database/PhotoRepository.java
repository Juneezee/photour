package com.photour.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.photour.helper.FutureHelper;
import com.photour.model.Photo;
import java.util.List;

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
   * @return LiveData<List < Photo>> List of Photos
   */
  public LiveData<List<Photo>> getAllLivePhotosDesc() {
    return FutureHelper.genericFuture(() -> photoDao.getAllDesc());
  }

  /**
   * Get all Photos from database
   *
   * @return List<Photo>
   */
  public List<Photo> getAllPhotos() {
    return FutureHelper.genericFuture(() -> photoDao.getAllPhotos());
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
