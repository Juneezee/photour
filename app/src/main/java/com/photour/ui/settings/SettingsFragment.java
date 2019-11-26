package com.photour.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.photour.R;

/**
 * Fragment that display the settings of the application
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class SettingsFragment extends Fragment {

  private SettingsViewModel settingsViewModel;

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

  /**
   * Called to have the fragment instantiate its user interface view.
   *
   * @param inflater The LayoutInflater object that can be used to inflate any views in the
   * fragment,
   * @param container If non-null, this is the parent view that the fragment's UI should be attached
   * to.  The fragment should not add the view itself, but this can be used to generate the
   * LayoutParams of the view.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   * @return View Return the View for the fragment's UI, or null.
   */
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    View root = inflater.inflate(R.layout.fragment_settings, container, false);
    final TextView textView = root.findViewById(R.id.text_notifications);
    settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    return root;
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
