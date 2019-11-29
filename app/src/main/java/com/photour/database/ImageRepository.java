package com.photour.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.photour.model.ImageElement;
import com.photour.model.Visit;
import java.util.List;

/**
 * Repository for Database
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ImageRepository {

  private AppDao appDao;
  private LiveData<List<ImageElement>> imageElements;

  public ImageRepository(Application application) {
    AppDatabase db = AppDatabase.getDatabase(application);
    appDao = db.ImageDao();
    imageElements = appDao.getAllDesc();
  }

  /**
   * Get a single image from the database
   *
   * @param id The ID of the image
   * @return {@link ImageElement} An {@link ImageElement} object
   */
  public ImageElement getImage(int id) {
    return imageElements.getValue().get(id);
  }

  /**
   * Gets all Images in database
   * @return List of ImageElements
   */
  public LiveData<List<ImageElement>> getAllImages() {
    return imageElements;
  }

  /**
   * Gets all visits in database
   * @return List of visits
   */
  public LiveData<List<Visit>> getVisits() {
    return appDao.getVisits();
  }

  /**
   * Gets all Images that is in the visit in database
   * @param visitTitle String title of the visit
   * @return List of ImageElement
   */
  public LiveData<List<ImageElement>> getImagesForVisit(String visitTitle) {
    return appDao.findByVisit(visitTitle);
  }

  /**
   * Adds ImageElement into the database
   * @param imageElements ImageElement object to be inserted into the database
   */
  void insert(ImageElement... imageElements) {
    AppDatabase.databaseWriteExecutor.execute(() -> {
      appDao.insertImages(imageElements);
    });
  }
}
