package com.photour.helper;

import android.content.Context;
import android.os.Environment;
import java.io.File;

public class Utils {
  public static final int IO_BUFFER_SIZE = 8 * 1024;

  private Utils() {};

  public static boolean isExternalStorageRemovable() {
    return Environment.isExternalStorageRemovable();
  }

  public static File getExternalCacheDir(Context context) {
    return context.getExternalCacheDir();
  }
}
