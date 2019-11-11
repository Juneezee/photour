package com.android.photour.ui.visit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.android.photour.R;

/**
 * Fragment for New Visit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitFragment extends Fragment {

  private VisitViewModel visitViewModel;

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
      Bundle savedInstanceState) {

    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);

    return inflater.inflate(R.layout.fragment_visit, container, false);
  }

  /**
   * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
   * but before any saved state has been restored in to the view.
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final TextView textViewDate = view.findViewById(R.id.text_date);
    visitViewModel.getTextDate().observe(getViewLifecycleOwner(), textViewDate::setText);

    startNewVisitListener(view);
  }

  /**
   * Add a click listener to the Start button, to replace current fragment with {@link
   * StartVisitFragment}
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void startNewVisitListener(View view) {
    final Button startButton = view.findViewById(R.id.button_start_visit);

    startButton.setOnClickListener(v -> {
      Fragment startVisitFragment = new StartVisitFragment();
      getParentFragmentManager().beginTransaction()
          .replace(R.id.nav_host_fragment, startVisitFragment)
          .addToBackStack(null)
          .commit();
    });
  }
}
