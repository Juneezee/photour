package com.android.photour.ui.visit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.android.photour.R;

public class VisitFragment extends Fragment {

  private VisitViewModel visitViewModel;

  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    visitViewModel = ViewModelProviders.of(this).get(VisitViewModel.class);
    View root = inflater.inflate(R.layout.fragment_visit, container, false);

    final TextView textViewTitle = root.findViewById(R.id.text_visit);
    final TextView textViewDate = root.findViewById(R.id.text_date);

    visitViewModel.getTextTitle().observe(this, textViewTitle::setText);
    visitViewModel.getTextDate().observe(this, textViewDate::setText);

    return root;
  }
}
