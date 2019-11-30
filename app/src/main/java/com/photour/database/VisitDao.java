package com.photour.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.photour.model.Visit;
import java.util.List;

/**
 * Data Access Object for Visits database
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Dao
public interface VisitDao {

  @Insert
  long insert(Visit visit);

  @Update
  void update(Visit visit);

  @Query("SELECT * FROM visits")
  LiveData<List<Visit>> getAllVisits();

  @Query("SELECT * FROM visits INNER JOIN photos ON visits.id = photos.visitId")
  Visit findByImage();
}
