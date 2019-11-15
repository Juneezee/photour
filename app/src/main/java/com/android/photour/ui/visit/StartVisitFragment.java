package com.android.photour.ui.visit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.MapFragment;
import com.google.android.libraries.maps.MapView;
import com.google.android.libraries.maps.OnMapReadyCallback;

/**
 * Fragment to create when new visit has started
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitFragment extends Fragment implements OnMapReadyCallback {

  private VisitViewModel visitViewModel;
  private Activity activity;

  private GoogleMap googleMap;
  private MapView mapView;

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
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
    activity = getActivity();

    return inflater.inflate(R.layout.fragment_start_visit, container, false);
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

    // Add click listener to stop button
    stopVisitListener(view);

    // Initialise chronometer
    initChronometer(view);

    // Prevent mini-slutter when the start button is pressed
    new Handler().post(() -> {
      view.findViewById(R.id.viewstub_map).setVisibility(View.VISIBLE);
    });

//    initGoogleMap();
  }

  /**
   * Add a click listener to the Stop button, to replace current fragment with {@link
   * VisitFragment}
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void stopVisitListener(View view) {
    final Button stopButton = view.findViewById(R.id.button_stop_visit);
    stopButton.setOnClickListener(
        v -> Navigation.findNavController(view).navigate(R.id.action_stop_visit));
  }

  /**
   * Initialise the chronometer
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  private void initChronometer(View view) {
    Chronometer chronometer = view.findViewById(R.id.chronometer);

    if (visitViewModel.getElapsedTime() == null) {
      // If the elapsed time is not defined, it's a new ViewModel so set it.
      long startTime = SystemClock.elapsedRealtime();
      visitViewModel.setElapsedTime(startTime);
      chronometer.setBase(startTime);
      System.out.println("Not Retained");
    } else {
      // Otherwise the ViewModel has been retained, set the chronometer's base to the original
      // starting time.
      System.out.println("Retained");
      chronometer.setBase(visitViewModel.getElapsedTime());
    }

    chronometer.start();
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
  }

  private void initGoogleMap() {
    mapView.getMapAsync(this);
  }

  /**
   * Called when the fragment is visible to the user and actively running.
   */
  @Override
  public void onResume() {
    super.onResume();
//    SupportMapFragment smf = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment));
    ((MainActivity) activity).setToolbarVisibility(false);
  }

  /**
   * Called when the Fragment is no longer started.
   */
  @Override
  public void onStop() {
    super.onStop();
    ((MainActivity) activity).setToolbarVisibility(true);
  }
}
