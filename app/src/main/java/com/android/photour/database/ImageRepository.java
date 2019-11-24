package com.android.photour.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.android.photour.model.ImageElement;

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
    imageElements = imageDao.getAll();
  }

  public LiveData<List<ImageElement>> getAllImages() {
    return imageElements;
  }

  void insert(ImageElement... imageElements) {
    AppDatabase.databaseWriteExecutor.execute(() -> {
      imageDao.insertImages(imageElements);
    });
  }
}
