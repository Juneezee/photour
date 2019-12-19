package com.photour.helper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

/**
 * A helper class for accessing the shared_preferences.xml
 *
 * @author Zer Jun Eng
 */
public class PreferenceHelper {

  public static void initializePreferences(Context context) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    setTheme(sharedPreferences);
    SharedPreferences.OnSharedPreferenceChangeListener listener =
        (sharedPreferences1, key) -> {
          if (key.equals("theme")) {
            setTheme(sharedPreferences1);
          }
        };
    sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
  }

  private static void setTheme(SharedPreferences sharedPreferences) {

    if (sharedPreferences.getBoolean("theme", false)) {
      AppCompatDelegate.setDefaultNightMode(
          AppCompatDelegate.MODE_NIGHT_YES);
    } else {
      AppCompatDelegate.setDefaultNightMode(
          AppCompatDelegate.MODE_NIGHT_NO);
    }
  }

  public static String tempUnit(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    return sharedPreferences.getString("temperature", "\u00B0C");
  }
}
