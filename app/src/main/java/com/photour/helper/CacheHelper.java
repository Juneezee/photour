package com.photour.helper;

import android.content.Context;
import android.os.Environment;
import java.io.File;

public class CacheHelper {

  public static final int IO_BUFFER_SIZE = 8 * 1024;

  public static boolean isExternalStorageRemovable() {
    return Environment.isExternalStorageRemovable();
  }

  public static File getExternalCacheDir(Context context) {
    return context.getExternalCacheDir();
  }

  public static String getImageIdString(String filepath) {
    String idStr = filepath.substring(filepath.lastIndexOf('/') + 1).replaceAll("[+() .]", "_")
        .toLowerCase();
    return idStr.substring(0, Math.min(idStr.length(), 64));
  }
}
