package com.android.photour.ui.photos;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
  private boolean mValid = true;
  private int mSectionResourceId;
  private int mTextResourceId;
  private RecyclerView.Adapter mBaseAdapter;
  private SparseArray<Section> mSections = new SparseArray<>();

  /**
   * Constructor class of SectionedGridRecyclerViewAdapter
   * @param context Context of MainActivity
   * @param sectionResourceId ID for view of section title
   * @param textResourceId ID for view of section items
   * @param recyclerView RecyclerView object being handled by adapter
   * @param baseAdapter Adapter responsible for dealing with the items
   */
  SectionedGridRecyclerViewAdapter(Context context, int sectionResourceId,
                                   int textResourceId, RecyclerView recyclerView,
                                   RecyclerView.Adapter baseAdapter) {

    mSectionResourceId = sectionResourceId;
    mTextResourceId = textResourceId;
    mBaseAdapter = baseAdapter;
    mContext = context;

    mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        mValid = mBaseAdapter.getItemCount() > 0;
        notifyDataSetChanged();
      }

      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) {
        mValid = mBaseAdapter.getItemCount() > 0;
        notifyItemRangeChanged(positionStart, itemCount);
      }

      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
        mValid = mBaseAdapter.getItemCount() > 0;
        notifyItemRangeInserted(positionStart, itemCount);
      }

      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) {
        mValid = mBaseAdapter.getItemCount() > 0;
        notifyItemRangeRemoved(positionStart, itemCount);
      }
    });

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
   * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an
   * item.
   *
   * Checks if typeView is title or item. If is item, call mBaseAdapter to handle.
   * @param parent he ViewGroup into which the new View will be added after it is bound to an
   *  adapter position.
   * @param typeView The view type of the new View.
   * @return
   */
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int typeView) {
    if (typeView == SECTION_TYPE) {
      final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
      return new SectionViewHolder(view, mTextResourceId);
    } else {
      return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
    }
  }

  /**
   * Called by RecyclerView to display the data at the specified position. This method should update
   * the contents of the itemView to reflect the item at the given position.
   *
   * Checks if given position is title or item. If is item, call mBaseAdapter to handle.
   * @param sectionViewHolder he ViewHolder which should be updated to represent the contents of the item at
   * the given position in the data set.
   * @param position The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder sectionViewHolder, int position) {
    if (isSectionHeaderPosition(position)) {
      ((SectionViewHolder) sectionViewHolder).title.setText(mSections.get(position).title);
    } else {
      mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
    }

  }

  /**
   * Getter for item view type.
   * Checks with SECTION_TYPE to see if said position is a title or item
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
   * @param position position of item
   * @return boolean True if the item is a title
   */
  private boolean isSectionHeaderPosition(int position) {
    return mSections.get(position) != null;
  }

  /**
   * Getter for id of item
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
    return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
  }

  /**
   * Class for viewHolder
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public static class SectionViewHolder extends RecyclerView.ViewHolder {

    TextView title;

    SectionViewHolder(View view, int mTextResourceid) {
      super(view);
      title = view.findViewById(mTextResourceid);
    }
  }

  /**
   * Class for section
   */
  static class Section {

    int firstPosition;
    int sectionedPosition;
    CharSequence title;

    Section(int firstPosition, CharSequence title) {
      this.firstPosition = firstPosition;
      this.title = title;
    }

  }

}
