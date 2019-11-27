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

  private ImageDao imageDao;
  private LiveData<List<ImageElement>> imageElements;

  public ImageRepository(Application application) {
    AppDatabase db = AppDatabase.getDatabase(application);
    imageDao = db.ImageDao();
    imageElements = imageDao.getAllDesc();
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

  public LiveData<List<ImageElement>> getAllImages() {
    return imageElements;
  }

  public LiveData<List<Visit>> getVisits() {
    return imageDao.getVisits();
  }

  public LiveData<List<ImageElement>> getImagesforVisit(String visitTitle) {
    return imageDao.findByVisit(visitTitle);
  }

  void insert(ImageElement... imageElements) {
    AppDatabase.databaseWriteExecutor.execute(() -> {
      imageDao.insertImages(imageElements);
    });
  }
}
