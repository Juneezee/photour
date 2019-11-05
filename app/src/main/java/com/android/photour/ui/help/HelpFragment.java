package com.android.photour.ui.help;

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

import com.android.photour.MainActivity;
import com.android.photour.R;

import java.util.Objects;

public class HelpFragment extends Fragment {

  private HelpViewModel pathsViewModel;

  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    pathsViewModel =
        ViewModelProviders.of(this).get(HelpViewModel.class);
    View root = inflater.inflate(R.layout.fragment_paths, container, false);
    final TextView textView = root.findViewById(R.id.text_notifications);
    pathsViewModel.getText().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
        textView.setText(s);
      }
    });
      ((MainActivity) Objects.requireNonNull(getActivity())).setNavigationVisibility(false);
    return root;
  }

    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) Objects.requireNonNull(getActivity())).setNavigationVisibility(true);
    }
}