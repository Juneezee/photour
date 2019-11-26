package com.photour.ui.photos;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.photour.databinding.FragmentPhotosSortBinding;
import java.util.Arrays;

/**
 * Adapter for handling sections and grid system on {@link PhotosFragment}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class SectionedGridRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int SECTION_TYPE = 0;
  private final Context mContext;
  private int mSectionResourceId;
  private int mTextResourceId;
  private PhotoAdapter mBaseAdapter;
  private SparseArray<Section> mSections = new SparseArray<>();

  /**
   * Constructor class of SectionedGridRecyclerViewAdapter
   *
   * @param context           Context of MainActivity
   * @param sectionResourceId ID for view of section title
   * @param textResourceId    ID for view of section items
   * @param recyclerView      RecyclerView object being handled by adapter
   * @param baseAdapter       Adapter responsible for dealing with the items
   */
  SectionedGridRecyclerViewAdapter(Context context, int sectionResourceId,
                                   int textResourceId, RecyclerView recyclerView,
                                   PhotoAdapter baseAdapter) {

    mSectionResourceId = sectionResourceId;
    mTextResourceId = textResourceId;
    mBaseAdapter = baseAdapter;
    mContext = context;

    mBaseAdapter.registerAdapterDataObserver(createObserver());

    final GridLayoutManager layoutManager = (GridLayoutManager) (recyclerView.getLayoutManager());
    if (layoutManager != null) {
      layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
          return (isSectionHeaderPosition(position)) ? layoutManager.getSpanCount() : 1;
        }
      });
    }
  }

  /**
   * Helper option to create adapterDataObserver
   */
  private RecyclerView.AdapterDataObserver createObserver() {
    return new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        notifyDataSetChanged();
      }

      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);
      }

      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
      }

      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
      }
    };
  }

  ;

  /**
   * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an
   * item.
   * <p>
   * Checks if typeView is title or item. If is item, call mBaseAdapter to handle.
   *
   * @param parent   The ViewGroup into which the new View will be added after it is bound to an
   *                 adapter position.
   * @param typeView The view type of the new View.
   * @return {@link RecyclerView.ViewHolder} The created viewholder
   */
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int typeView) {
    if (typeView == SECTION_TYPE) {
      FragmentPhotosSortBinding fragmentPhotosSortBinding = DataBindingUtil.inflate(
              LayoutInflater.from(mContext), mSectionResourceId, parent, false);
      return new SectionViewHolder(fragmentPhotosSortBinding);
    } else {
      return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
    }
  }

  /**
   * Called by RecyclerView to display the data at the specified position. This method should update
   * the contents of the itemView to reflect the item at the given position.
   * <p>
   * Checks if given position is title or item. If is item, call mBaseAdapter to handle.
   *
   * @param sectionViewHolder the ViewHolder which should be updated to represent the contents of the item at
   *                          the given position in the data set.
   * @param position          The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder sectionViewHolder, int position) {
    if (isSectionHeaderPosition(position)) {
      Section section = mSections.get(position);
      ((SectionViewHolder) sectionViewHolder).fragmentPhotosSortBinding.setTitle(section);
      ((SectionViewHolder) sectionViewHolder).fragmentPhotosSortBinding.executePendingBindings();
    } else {
      mBaseAdapter.onBindViewHolder((PhotoAdapter.ImageCard) sectionViewHolder, sectionedPositionToPosition(position));
    }
  }

  /**
   * Getter for item view type.
   * Checks with SECTION_TYPE to see if said position is a title or item
   *
   * @param position position of current item on data set
   * @return Type of item, 0 represents HEADER
   */
  @Override
  public int getItemViewType(int position) {
    return isSectionHeaderPosition(position)
            ? SECTION_TYPE
            : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
  }

  /**
   * Accessor for sections array. Used to update data set
   *
   * @param sections Array of sections
   */
  void setSections(Section[] sections) {
    mSections.clear();

    Arrays.sort(sections, (o, o1) -> Integer.compare(o.firstPosition, o1.firstPosition));

    int offset = 0; // offset positions for the headers we're adding
    for (Section section : sections) {
      section.sectionedPosition = section.firstPosition + offset;
      mSections.append(section.sectionedPosition, section);
      ++offset;
    }

    notifyDataSetChanged();
  }

  /**
   * Checks the position of next title
   *
   * @param sectionedPosition position of a current title
   * @return int position of next title
   */
  private int sectionedPositionToPosition(int sectionedPosition) {
    if (isSectionHeaderPosition(sectionedPosition)) {
      return RecyclerView.NO_POSITION;
    }

    int offset = 0;
    for (int i = 0; i < mSections.size(); i++) {
      if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
        break;
      }
      --offset;
    }
    return sectionedPosition + offset;
  }

  /**
   * Checks if item in position is a title
   *
   * @param position position of item
   * @return boolean True if the item is a title
   */
  private boolean isSectionHeaderPosition(int position) {
    return mSections.get(position) != null;
  }

  /**
   * Getter for id of item
   *
   * @param position position of item
   * @return long ID of the item
   */
  @Override
  public long getItemId(int position) {
    return isSectionHeaderPosition(position)
            ? Integer.MAX_VALUE - mSections.indexOfKey(position)
            : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
  }

  /**
   * Getter for data set size
   *
   * @return int Size of the data set
   */
  @Override
  public int getItemCount() {
    return mBaseAdapter.getItemCount() > 0 ? mBaseAdapter.getItemCount() + mSections.size() : 0;
  }

  /**
   * Class for viewHolder
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public static class SectionViewHolder extends RecyclerView.ViewHolder {

    //    TextView title;
    private FragmentPhotosSortBinding fragmentPhotosSortBinding;

    SectionViewHolder(@NonNull FragmentPhotosSortBinding fragmentPhotosSortBinding) {
      super(fragmentPhotosSortBinding.getRoot());
      this.fragmentPhotosSortBinding = fragmentPhotosSortBinding;
    }
  }

  /**
   * Class for section
   */
  public static class Section {

    int firstPosition;
    int sectionedPosition;
    String title;

    Section(int firstPosition, String title) {
      this.firstPosition = firstPosition;
      this.title = title;
    }

    public String getTitle() {
      return this.title;
    }
  }
}
