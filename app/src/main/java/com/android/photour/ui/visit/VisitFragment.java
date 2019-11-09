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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import com.android.photour.MainActivity;
import com.android.photour.R;
import com.google.android.libraries.maps.MapFragment;
import com.google.android.libraries.maps.SupportMapFragment;
import java.util.Objects;

public class VisitFragment extends Fragment {

  private VisitViewModel visitViewModel;

  /**
   * @param inflater c
   * @param container c
   * @param savedInstanceState c
   * @return View
   */
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    visitViewModel = ViewModelProviders.of(this).get(VisitViewModel.class);

    return inflater.inflate(R.layout.fragment_visit, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final TextView textViewTitle = view.findViewById(R.id.text_visit);
    final TextView textViewDate = view.findViewById(R.id.text_date);

    visitViewModel.getTextTitle().observe(this, textViewTitle::setText);
    visitViewModel.getTextDate().observe(this, textViewDate::setText);

    startNewVisitListener(view);
  }

  /**
   * @param root c
   */
  private void startNewVisitListener(View root) {
    final Button startButton = root.findViewById(R.id.button_start_visit);

    startButton.setOnClickListener(v -> {
      Fragment startVisitFragment = new StartVisitFragment();
      assert getFragmentManager() != null;
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.nav_host_fragment, startVisitFragment);
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction.commit();
    });
  }
}
