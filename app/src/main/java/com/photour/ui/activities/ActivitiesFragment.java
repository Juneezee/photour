package com.photour.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.photour.R;

public class ActivitiesFragment extends Fragment {

  private ActivitiesViewModel activitiesViewModel;

  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    activitiesViewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);
    View root = inflater.inflate(R.layout.fragment_visits, container, false);
    final TextView textView = root.findViewById(R.id.text_notifications);
    activitiesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    return root;
  }
}
