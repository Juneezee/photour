package com.android.photour.database;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.android.photour.helper.DateConverter;
import com.android.photour.model.ImageElement;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database class.
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Database(entities = {ImageElement.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
  public abstract ImageDao ImageDao();

  private static volatile AppDatabase INSTANCE;
  private static final int NUMBER_OF_THREADS = 4;
  static final ExecutorService databaseWriteExecutor =
          Executors.newFixedThreadPool(NUMBER_OF_THREADS);
  private Context context;

  /**
   * Initialise the database if not yet initialised, else return the database object.
   * @param context Context of MainActivity
   * @return AppDatabase the database object
   */
  public static AppDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                  AppDatabase.class, "app_database")
                  .addCallback(sRoomDatabaseCallback)
                  .build();
          INSTANCE.context = context;
        }
      }
    }
    return INSTANCE;
  }

  /**
   * Callback function to seed the database.
   * Used for testing.
   */
  private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
      super.onOpen(db);
      // If you want to keep data through app restarts,
      // comment out the following block
//      databaseWriteExecutor.execute(() -> {
//
//        // Populate the database in the background.
//        // If you want to start with more words, just add them.
//        ImageDao dao = INSTANCE.ImageDao();
//        dao.deleteAll();
//
//        String[] projection = new String[]{MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DISPLAY_NAME,
//                MediaStore.Images.Media.DATE_TAKEN,
//                "_data"
//        };
//
//        String selection = "( _data LIKE ? )";
//        String[] selectionArgs = new String[]{"%Pictures%"};
//
//        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
//
//        Cursor cursor = INSTANCE.context.getContentResolver().query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder
//        );
//
//        if (cursor == null) {
//          return;
//        }
//
//        while (cursor.moveToNext()) {
//          long columnIndex = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//          Uri contentUri = ContentUris.withAppendedId(
//                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columnIndex);
//          String pathname = getPath(cursor.getString(cursor.getColumnIndexOrThrow("_data")));
//          long dateTaken = cursor.getLong(
//                  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
//          Calendar calendar = Calendar.getInstance();
//          calendar.setTimeInMillis(dateTaken);
//          Date date = calendar.getTime();
//          ImageElement imageElement =
//                  new ImageElement(contentUri.toString(), pathname, 53.3808641,-1.4877637, 0, 0, date);
//          dao.insertImages(imageElement);
//
//        }
//
//        cursor.close();
//      });
    }
  };

  /**
   * Helper class to get name of folder from whole path
   *
   * @param dir Full directory path
   * @return String folder name where the file sits
   */
  private static String getPath(String dir) {
    String[] temp = dir.split("/");
    return temp[temp.length - 2];
  }
}
