package com.photour.ui.photos.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.databinding.FragmentPhotosClusterBinding;
import com.photour.model.Photo;
import com.photour.ui.photos.PhotoAdapter;
import com.photour.ui.photos.PhotosViewModel;
import java.util.Arrays;

/**
 * Fragment for viewing cluster photos
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class PhotosClusterFragment extends Fragment {

  private FragmentPhotosClusterBinding binding;
  private Activity activity;

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
  }

  /**
   * Called to have the fragment instantiate its user interface view. Permission for storage access
   * is handled here
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
    binding = FragmentPhotosClusterBinding.inflate(inflater, container, false);

    return binding.getRoot();
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
    initRecyclerView();
  }

  /**
   * Initialise recycler view for cluster photos
   */
  private void initRecyclerView() {
    if (getArguments() == null) {
      return;
    }

    Photo[] photos = PhotosClusterFragmentArgs.fromBundle(getArguments()).getPhotos();

    if (photos == null) {
      return;
    }

    final RecyclerView recyclerView = binding.gridRecyclerView;
    final int IMAGE_WIDTH = 100;
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new GridLayoutManager(
        activity, PhotosViewModel.calculateNoOfColumns(activity, IMAGE_WIDTH)));

    PhotoAdapter photoAdapter = new PhotoAdapter();
    photoAdapter.setItems(Arrays.asList(photos));

    recyclerView.setAdapter(photoAdapter);
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
