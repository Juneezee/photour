package com.photour.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.LruCache;
import com.photour.MainActivity;
import com.photour.database.DiskLruImageCache;
import com.photour.ui.photos.PhotosFragment;
import java.io.File;

/**
 * A helper class for managing cache for photos
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class CacheHelper {

  private DiskLruImageCache diskLruCache;
  private final Object diskCacheLock;
  private boolean diskCacheStarting;
  private Context context;

  private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
  private static final String DISK_CACHE_SUBDIR = "thumbnails";

  private LruCache<String, Bitmap> memoryCache;

  public static final int IO_BUFFER_SIZE = 8 * 1024;

  /**
   * Constructor for CacheHelper Initialises Memory Cache and Disk Cache
   *
   * @param context The context of current application
   */
  public CacheHelper(Context context) {
    // Sets up variables
    diskCacheLock = new Object();
    diskCacheStarting = true;
    this.context = context;

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 4;

    //Creates or find PhotosFragment sets memory cache of PhotosFragment
    PhotosFragment mRetainFragment =
        PhotosFragment
            .findOrCreateRetainFragment(((MainActivity) context).getSupportFragmentManager());
    memoryCache = PhotosFragment.mRetainedCache;
    if (memoryCache == null) {
      memoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
          return bitmap.getByteCount() / 1024;
        }
      };
      PhotosFragment.mRetainedCache = memoryCache;
    }

    //Runs Async Task for Disk Cache
    new InitDiskCacheTask().execute(DISK_CACHE_SUBDIR);
  }

  /**
   * Function to add bitmap to memory and disk cache
   *
   * @param key Key to identify bitmap
   * @param bitmap bitmap object itself
   */
  public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
      memoryCache.put(key, bitmap);
    }

    synchronized (diskCacheLock) {
      if (diskLruCache != null && diskLruCache.getBitmap(key) == null) {
        diskLruCache.put(key, bitmap);
      }
    }
  }

  /**
   * Function to get bitmap from memory cache
   *
   * @param key Key to identify bitmap
   * @return bitmap object. Returns null if invalid
   */
  public Bitmap getBitmapFromMemCache(String key) {
    return memoryCache.get(key);
  }

  /**
   * Function to get bitmap from disk cache. Runs in async due to slow speed of disk cache.
   *
   * @param key Key to identify bitmap
   * @return bitmap object. Returns null if invalid
   */
  public Bitmap getBitmapFromDiskCache(String key) {
    synchronized (diskCacheLock) {
      // Wait while disk cache is started from background thread
      while (diskCacheStarting) {
        try {
          diskCacheLock.wait();
        } catch (InterruptedException ignored) {
        }
      }
      if (diskLruCache != null) {
        return diskLruCache.getBitmap(key);
      }
    }
    return null;
  }

  /**
   * Async task class to initialise disk cache
   */
  class InitDiskCacheTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
      synchronized (diskCacheLock) {
        String cacheDir = params[0];
        diskLruCache = new DiskLruImageCache(context, cacheDir, DISK_CACHE_SIZE);

        diskCacheStarting = false; // Finished initialization
        diskCacheLock.notifyAll(); // Wake any waiting threads
      }
      return null;
    }
  }

  /**
   * Returns boolean if external storage is removable
   *
   * @return boolean {@code true} if external storage is removable
   */
  public static boolean isExternalStorageRemovable() {
    return Environment.isExternalStorageRemovable();
  }

  /**
   * Accessor for absolute path of external storage directory
   *
   * @param context context of MainActivty
   * @return A {@link File} object
   */
  public static File getExternalCacheDir(Context context) {
    return context.getExternalCacheDir();
  }

  /**
   * Function to convert filename to key that disk cache accepts
   *
   * @param filepath file name of image
   * @return String key for image
   */
  public static String getImageIdString(String filepath) {
    String idStr = filepath.substring(filepath.lastIndexOf('/') + 1).replaceAll("[+() .@]", "_")
        .toLowerCase();
    return idStr.substring(0, Math.min(idStr.length(), 64));
  }
}
