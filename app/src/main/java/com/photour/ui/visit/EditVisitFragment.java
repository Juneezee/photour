package com.photour.ui.visit;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.photour.databinding.FragmentVisitEditBinding;
import com.photour.helper.ToastHelper;
import com.photour.model.Visit;

/**
 * Fragment for edit visit
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class EditVisitFragment extends Fragment {

  private Activity activity;
  private VisitViewModel visitViewModel;
  private FragmentVisitEditBinding binding;

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

    activity = getActivity();
    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
  }

  /**
   * Called to have the fragment instantiate its user interface view. Visit argument is parsed here
   * and loads photos.
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
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    super.onCreateView(inflater, container, savedInstanceState);

    binding = FragmentVisitEditBinding.inflate(inflater, container, false);
    binding.setFragment(this);

    if (getArguments() != null) {
      Visit visit = EditVisitFragmentArgs.fromBundle(getArguments()).getVisit();
      visitViewModel.visit = visit;
      binding.setVisit(visit);
    }

    return binding.getRoot();
  }

  /**
   * Update the title of the visit
   */
  public void updateVisitTitle() {
    Editable editText = binding.editVisitTitleInput.getText();

    if (editText == null) {
      return;
    }

    if (visitViewModel.updateVisitTitle(editText.toString())) {
      Navigation.findNavController(binding.getRoot()).navigateUp();
    } else {
      ToastHelper.tShort(activity, "Failed to update visit title");
    }
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
