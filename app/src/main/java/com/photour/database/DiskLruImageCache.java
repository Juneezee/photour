package com.photour.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;
import com.photour.BuildConfig;
import com.photour.helper.CacheHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class for creating and storing disk cache
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class DiskLruImageCache {

  private static final int APP_VERSION = 1;
  private static final int VALUE_COUNT = 1;
  private static final String TAG = DiskLruImageCache.class.getSimpleName();

  private DiskLruCache mDiskCache;
  private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;

  /**
   * Constructor for DiskLruImageCache.
   *
   * @param context Context of MainActivity
   * @param uniqueName Name for the disk cache
   * @param diskCacheSize Size of diskcache
   */
  public DiskLruImageCache(Context context, String uniqueName, int diskCacheSize) {
    try {
      final File diskCacheDir = getDiskCacheDir(context, uniqueName);
      mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Function to convert bitmap to compressed file
   *
   * @param bitmap Bitmap object that needs to be converted
   * @param editor Editor object
   * @return True if the compression is successful else return false
   * @throws IOException Exeception if unable to access storage
   */
  private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException {
    try (OutputStream out = new BufferedOutputStream(editor.newOutputStream(0),
        CacheHelper.IO_BUFFER_SIZE)) {
      int mCompressQuality = 70;
      return bitmap.compress(mCompressFormat, mCompressQuality, out);
    }
  }

  /**
   * Function to get directory of disk cache
   *
   * @param context context Context of MainActivity
   * @param uniqueName name of cache
   * @return File generated from path to disk cache
   */
  private File getDiskCacheDir(Context context, String uniqueName) {

    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
    // otherwise use internal cache dir
    final String cachePath =
        Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
            !CacheHelper.isExternalStorageRemovable() ?
            CacheHelper.getExternalCacheDir(context).getPath() :
            context.getCacheDir().getPath();

    return new File(cachePath + File.separator + uniqueName);
  }

  /**
   * Function to insert bitmap to disk cache
   *
   * @param key String key to find bitmap
   * @param data the Bitmap object itself
   */
  public void put(String key, Bitmap data) {

    DiskLruCache.Editor editor = null;
    try {
      editor = mDiskCache.edit(key);
      if (editor == null) {
        return;
      }
      if (writeBitmapToFile(data, editor)) {
        mDiskCache.flush();
        editor.commit();
        if (BuildConfig.DEBUG) {
          Log.d(TAG, "image put on disk cache " + key);
        }
      } else {
        editor.abort();
        if (BuildConfig.DEBUG) {
          Log.d(TAG, "ERROR on: image put on disk cache " + key);
        }
      }
    } catch (IOException e) {
      if (BuildConfig.DEBUG) {
        Log.d(TAG, "ERROR on: image put on disk cache " + key);
      }
      try {
        if (editor != null) {
          editor.abort();
        }
      } catch (IOException ignored) {
      }
    }
  }

  /**
   * Accessor to retrieve bitmap from disk cache
   *
   * @param key String to identify Bitmap
   * @return Bitmap object or null if the Bitmap doesn't exist
   */
  public Bitmap getBitmap(String key) {

    Bitmap bitmap = null;
    try (DiskLruCache.Snapshot snapshot = mDiskCache.get(key)) {

      if (snapshot == null) {
        return null;
      }
      final InputStream in = snapshot.getInputStream(0);
      if (in != null) {
        final BufferedInputStream buffIn = new BufferedInputStream(in, CacheHelper.IO_BUFFER_SIZE);
        bitmap = BitmapFactory.decodeStream(buffIn);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (BuildConfig.DEBUG) {
      Log.d(TAG, bitmap == null ? "" : "image read from disk " + key);
    }

    return bitmap;
  }

  /**
   * Check if disk cache contains a bitmap
   *
   * @param key String to indentify Bitmap
   * @return True if the bitmap is in the cache, else False
   */
  public boolean containsKey(String key) {

    boolean contained = false;
    try (DiskLruCache.Snapshot snapshot = mDiskCache.get(key)) {
      contained = snapshot != null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return contained;
  }

}
