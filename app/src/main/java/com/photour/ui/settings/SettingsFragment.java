package com.photour.ui.settings;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.photour.R;

/**
 * Fragment that display the settings of the application
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class SettingsFragment extends PreferenceFragmentCompat {

  /**
   * Called to do initial creation of a fragment.  This is called after * {@link
   * #onAttach(Activity)} and before * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   * is the state.
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }


  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.fragment_settings, rootKey);
  }

  /**
   * Initialize the contents of the Fragment host's standard options menu. Menu items should be
   * placed in <var>menu</var>.  For this method to be called, it must have first called {@link
   * #setHasOptionsMenu}.
   *
   * @param menu The options menu in which you place your items. shown.
   */
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    // Do not show the settings icon once we are in settings fragment
    menu.clear();
  }
}