package com.android.photour.ui.photos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.photour.ImageElement;
import com.android.photour.R;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class PhotosFragment extends Fragment {
  private static final String TAG = "PhotosFragment";
  public static LruCache<String, Bitmap> mRetainedCache;
  private PhotosViewModel photosViewModel;
  private List<ImageElement> pictureList = new ArrayList<ImageElement>();
  private PhotoAdapter photoAdapter;
  private RecyclerView mRecyclerView;
  private final int IMAGE_WIDTH = 100;


  public static PhotosFragment findOrCreateRetainFragment(FragmentManager fm) {
    PhotosFragment fragment = (PhotosFragment) fm.findFragmentByTag(TAG);
    if (fragment == null) {
      fragment = new PhotosFragment();
    }
    return fragment;
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    View root = inflater.inflate(R.layout.fragment_photos, container, false);

    photosViewModel.images.observe(getViewLifecycleOwner(), imageElements -> {
      photoAdapter.setItems(imageElements);
      photoAdapter.notifyDataSetChanged();
    });

    mRecyclerView = root.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    photoAdapter = new PhotoAdapter(getContext());
    mRecyclerView.setAdapter(photoAdapter);

    return root;
  }

}
