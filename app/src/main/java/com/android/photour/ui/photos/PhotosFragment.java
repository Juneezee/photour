package com.android.photour.ui.photos;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.photour.ImageElement;
import com.android.photour.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotosFragment extends Fragment {
  private static final String TAG = "PhotosFragment";
  public static LruCache<String, Bitmap> mRetainedCache;
  private PhotosViewModel photosViewModel;
  private List<ImageElement> pictureList = new ArrayList<ImageElement>();
  private PhotoAdapter photoAdapter;
  private RecyclerView mRecyclerView;
  private FloatingActionButton sortButton;
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

    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<Uri> uris = new ArrayList<>();

    mRecyclerView = root.findViewById(R.id.grid_recycler_view);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
            PhotosViewModel.calculateNoOfColumns(Objects.requireNonNull(getContext()), IMAGE_WIDTH)));

    photoAdapter = new PhotoAdapter(getContext());
    SectionedGridRecyclerViewAdapter.Section[] dummy =
            new SectionedGridRecyclerViewAdapter.Section[sections.size()];
    SectionedGridRecyclerViewAdapter mSectionedAdapter = new
            SectionedGridRecyclerViewAdapter(getActivity(),
            R.layout.fragment_photos_sort,R.id.sorted_title_view,mRecyclerView,photoAdapter);

    photosViewModel.images.observe(getViewLifecycleOwner(), imageElements -> {
      System.out.println("OBSERVER TRIGGERED");
      System.out.println(imageElements.size());
      sections.clear();
      uris.clear();

      int pos = 0;
      for (ImageElement imageElement : imageElements) {
        sections.add(new SectionedGridRecyclerViewAdapter.Section(pos,imageElement.getTitle()));
        pos += imageElement.getUris().size(); // add number of photos and title
        uris.addAll(imageElement.getUris());
      }
      photoAdapter.setItems(uris);
      photoAdapter.notifyDataSetChanged();
      mSectionedAdapter.notifyDataSetChanged();
      mSectionedAdapter.setSections(sections.toArray(dummy));
      mRecyclerView.setAdapter(mSectionedAdapter);
    });

    sortButton = root.findViewById(R.id.fab_sort);
    initializeSortButton();

    return root;
  }

  private void initializeSortButton() {
    if (photosViewModel.sortMode == 0) {
      sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar, null));
    } else {
      sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map, null));
    }
    sortButton.setOnClickListener(v -> {
      photosViewModel.switchSortMode();
      if (photosViewModel.sortMode == 0) {
        sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar, null));
      } else {
        sortButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map, null));
      }
    });
  }

}
