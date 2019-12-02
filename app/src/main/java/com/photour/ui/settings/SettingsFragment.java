package com.photour.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import com.photour.R;

/**
 * Fragment that display the settings of the application
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class SettingsFragment extends PreferenceFragmentCompat {

  /**
   * Called to do initial creation of a fragment.  This is called after {@link #onAttach(Activity)}
   * and before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   * is the state.
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  /**
   * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   * is the state.
   * @param rootKey If non-null, this preference fragment should be rooted at the {@link
   * PreferenceScreen} with this key.
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.fragment_settings, rootKey);
  }

  /**
   * Prepare the Fragment host's standard options menu to be displayed.
   *
   * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
   * @see #setHasOptionsMenu
   * @see #onCreateOptionsMenu
   */
  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    // Do not show any menu items
    menu.clear();
  }
}
