package com.photour.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.photour.converter.DateConverter;
import com.photour.converter.FloatArrayConverter;
import com.photour.converter.LatLngConverter;
import com.photour.converter.LatLngListConverter;
import com.photour.model.Photo;
import com.photour.model.Visit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database class
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Database(entities = {Photo.class, Visit.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, FloatArrayConverter.class})
public abstract class AppDatabase extends RoomDatabase {

  public abstract VisitDao visitDao();

  public abstract PhotoDao imageDao();

  private static volatile AppDatabase INSTANCE;
  private static final int NUMBER_OF_THREADS = 4;
  static final ExecutorService databaseWriteExecutor = Executors
      .newFixedThreadPool(NUMBER_OF_THREADS);

  /**
   * Initialise the database if not yet initialised, else return the database object.
   *
   * @param context Context of MainActivity
   * @return AppDatabase the database object
   */
  static synchronized AppDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      INSTANCE = Room
          .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database")
          .build();
    }
    return INSTANCE;
  }
}
