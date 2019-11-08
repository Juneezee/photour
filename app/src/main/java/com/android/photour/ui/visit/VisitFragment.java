package com.android.photour.ui.visit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import com.android.photour.R;
import java.util.Objects;

public class VisitFragment extends Fragment {

  private VisitViewModel visitViewModel;

  /**
   *
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return View
   */
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    visitViewModel = ViewModelProviders.of(this).get(VisitViewModel.class);
    View root = inflater.inflate(R.layout.fragment_visit, container, false);

    initView(root);
    startNewVisitListener(root);

    return root;
  }

  /**
   *
   * @param root c
   */
  private void initView(View root) {
    final TextView textViewTitle = root.findViewById(R.id.text_visit);
    final TextView textViewDate = root.findViewById(R.id.text_date);

    visitViewModel.getTextTitle().observe(this, textViewTitle::setText);
    visitViewModel.getTextDate().observe(this, textViewDate::setText);
  }

  /**
   *
   * @param root c
   */
  private void startNewVisitListener(View root) {
    final Button startButton =  root.findViewById(R.id.button_start_visit);

    startButton.setOnClickListener(v -> {
      StartVisitFragment startVisitFragment = new StartVisitFragment();
      assert getFragmentManager() != null;
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.nav_host_fragment, startVisitFragment);
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction.commit();
    });
  }
}
