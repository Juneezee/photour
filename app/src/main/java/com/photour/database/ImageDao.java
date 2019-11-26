package com.photour.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.photour.model.ImageElement;
import com.photour.model.TripElement;
import java.util.Date;
import java.util.List;

/**
 * Data Acesss Object for Database
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Dao
public interface ImageDao {

  @Query("SELECT * FROM image_element ORDER BY date DESC")
  LiveData<List<ImageElement>> getAllDesc();

  @Query("SELECT * FROM image_element ORDER BY date ASC")
  LiveData<List<ImageElement>> getAllAsc();

  @Query("SELECT visit_title, relative_path, COUNT(*) AS photoNo FROM image_element GROUP BY visit_title ORDER BY date DESC")
  LiveData<List<TripElement>> getTrips();

  @Query("SELECT * FROM image_element WHERE id IN (:ids)")
  LiveData<List<ImageElement>> loadAllByIds(int[] ids);

  @Query("SELECT * FROM image_element WHERE visit_title LIKE :visit")
  LiveData<List<ImageElement>> findByVisit(String visit);

  @Query("SELECT * FROM image_element WHERE date LIKE :date")
  LiveData<List<ImageElement>> findByDate(Date date);

  @Insert
  void insertImages(ImageElement... images);

  @Delete
  void delete(ImageElement image);

  @Query("DELETE FROM image_element")
  void deleteAll();
}
