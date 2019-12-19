package com.photour.ui.photo;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.photour.R;

/**
 * A class for handling bottom sheet on {@link ZoomImageView}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
class BottomSheet {

  private BottomSheetBehavior bottomSheetBehavior = null;

  /**
   * Set the bottom sheet behaviour
   *
   * @param b A {@link BottomSheetBehavior} object
   */
  void setBottomSheet(TextView detailTag, BottomSheetBehavior b) {
    bottomSheetBehavior = b;

    bottomSheetBehavior.addBottomSheetCallback(new BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
          detailTag
              .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_down, 0, 0);
        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
          detailTag
              .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_up, 0, 0);
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
      }
    });
  }

  void setBehaviorState(int state) {
    bottomSheetBehavior.setState(state);
  }

  void toggleBehaviorState() {
    if (isExpanded()) {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    } else if (isHidden() || isCollapsed()) {
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  /**
   * Check whether the <var>bottomSheetBehavior</var> is null or not
   *
   * @return boolean {@code true} if bottomSheetBehavior is not null, {@code false} otherwise
   */
  boolean behaviorIsNotNull() {
    return bottomSheetBehavior != null;
  }

  /**
   * Check is bottom sheet is expanded or not
   *
   * @return boolean {@code true} if the bottom sheet is expanded, {@code false} otherwise
   */
  boolean isExpanded() {
    return behaviorIsNotNull()
        && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
  }

  boolean isHidden() {
    return behaviorIsNotNull()
        && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN;
  }

  boolean isCollapsed() {
    return behaviorIsNotNull()
        && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
  }
}
