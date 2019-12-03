package com.photour.ui.photo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.OverScroller;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Customised {@link AppCompatImageView} to support pinch zoom, tap zoom, and panning
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ZoomImageView extends AppCompatImageView {

  // Touch event states
  private enum State {NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM}

  private State state;

  // SUPER_MIN and SUPER_MAX multipliers. Determine how much the image can be
  // zoomed below or above the zoom boundaries, before animating back to the
  // min/max zoom boundary.
  private static final float SUPER_MIN_MULTIPLIER = .75f;
  private static final float SUPER_MAX_MULTIPLIER = 1.25f;

  private static final float MIN_SCALE = 1;
  private static final float MAX_SCALE = 3;
  private static final float superMinScale = SUPER_MIN_MULTIPLIER * MIN_SCALE;
  private static final float superMaxScale = SUPER_MAX_MULTIPLIER * MAX_SCALE;

  // Scale of image ranges from MIN_SCALE to MAX_SCALE, where MIN_SCALE == 1
  // when the image is stretched to fit view.
  private float normalisedScale = 1;

  // Matrix applied to image. MSCALE_X and MSCALE_Y should always be equal.
  // MTRANS_X and MTRANS_Y are the other values used. prevMatrix is the matrix
  // saved prior to the screen rotating.
  private Matrix matrix = new Matrix();
  private Matrix prevMatrix = new Matrix();
  private float[] m = new float[9];

  private Context context;
  private Fling fling;

  private ScaleType mScaleType;

  private boolean imageRenderedAtLeastOnce;
  private boolean onDrawReady;

  private ZoomVariables delayedZoomVariables;

  // Size of view and previous view size (ie before rotation)
  private int viewWidth, viewHeight, prevViewWidth, prevViewHeight;

  // Size of image when it is stretched to fit view. Before and After rotation.
  private float matchViewWidth, matchViewHeight, prevMatchViewWidth, prevMatchViewHeight;

  private ScaleGestureDetector mScaleDetector;
  private GestureDetector mGestureDetector;
  private GestureDetector.OnDoubleTapListener doubleTapListener = null;
  private OnTouchListener userTouchListener = null;
  private OnZoomImageViewListener zoomImageViewListener = null;

  /**
   * Constructor of {@link ZoomImageView}
   *
   * @param context The application context
   */
  public ZoomImageView(Context context) {
    super(context);
    sharedConstructor(context);
  }

  /**
   * Constructor of {@link ZoomImageView}
   *
   * @param context The application context
   * @param attrs A collection of attributes in XML
   */
  public ZoomImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    sharedConstructor(context);
  }

  /**
   * Constructor of {@link ZoomImageView}
   *
   * @param context The application context
   * @param attrs A collection of attributes in XM
   * @param defStyle Default style attribute
   */
  public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    sharedConstructor(context);
  }

  /**
   * A shared method for the constructors to initialise instance variables
   *
   * @param context The application context
   */
  private void sharedConstructor(Context context) {
    super.setClickable(true);
    this.context = context;
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    mGestureDetector = new GestureDetector(context, new GestureListener());
    if (mScaleType == null) {
      mScaleType = ScaleType.FIT_CENTER;
    }
    setImageMatrix(matrix);
    setScaleType(ScaleType.MATRIX);
    setState(State.NONE);
    super.setOnTouchListener(new PrivateOnTouchListener());
  }

  /**
   * Register a callback to be invoked when a touch event is sent to this view.
   *
   * @param l the touch listener to attach to this view
   */
  @Override
  public void setOnTouchListener(OnTouchListener l) {
    userTouchListener = l;
  }

  /**
   * Sets a drawable as the content of this ImageView.
   *
   * <p>Allows the use of vector drawables when running on older versions of the platform.</p>
   *
   * @param resId the resource identifier of the drawable
   * @see ImageView#setImageResource(int)
   */
  @Override
  public void setImageResource(int resId) {
    super.setImageResource(resId);
    savePreviousImageValues();
    fitImageToView();
  }


  /**
   * Sets a Bitmap as the content of this ImageView.
   *
   * @param bm The bitmap to set
   * @see ImageView#setImageBitmap(Bitmap) (int)
   */
  @Override
  public void setImageBitmap(Bitmap bm) {
    super.setImageBitmap(bm);
    savePreviousImageValues();
    fitImageToView();
  }

  /**
   * Sets a drawable as the content of this ImageView.
   *
   * @param drawable the Drawable to set, or {@code null} to clear the content
   * @see ImageView#setImageDrawable(Drawable)
   */
  @Override
  public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);
    savePreviousImageValues();
    fitImageToView();
  }

  /**
   * Controls how the image should be resized or moved to match the size of this ImageView.
   *
   * @param type The desired scaling mode.
   * @see ImageView#setScaleType(ScaleType)
   */
  @Override
  public void setScaleType(ScaleType type) {
    if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
      throw new UnsupportedOperationException(
          "ZoomImageView does not support FIT_START or FIT_END");
    }

    if (type == ScaleType.MATRIX) {
      super.setScaleType(ScaleType.MATRIX);

    } else {
      mScaleType = type;
      if (onDrawReady) {
        // If the image is already rendered, scaleType has been called programmatically
        // and the ZoomImageView should be updated with the new scaleType.
        setZoom(this);
      }
    }
  }

  /**
   * Returns the current ScaleType that is used to scale the bounds of an image to the bounds of the
   * ImageView.
   *
   * @return The ScaleType used to scale the image.
   * @see ImageView.ScaleType
   * @see ImageView#getScaleType()
   */
  @Override
  public ScaleType getScaleType() {
    return mScaleType;
  }

  /**
   * Returns false if image is in initial, unzoomed state. True, otherwise.
   *
   * @return boolean {@code true} if image is zoomed, false otherwise
   */
  public boolean isZoomed() {
    return normalisedScale != 1;
  }

  /**
   * Save the current matrix and view dimensions in the prevMatrix and prevView variables.
   */
  private void savePreviousImageValues() {
    if (matrix != null && viewHeight != 0 && viewWidth != 0) {
      matrix.getValues(m);
      prevMatrix.setValues(m);
      prevMatchViewHeight = matchViewHeight;
      prevMatchViewWidth = matchViewWidth;
      prevViewHeight = viewHeight;
      prevViewWidth = viewWidth;
    }
  }

  /**
   * Hook allowing a view to generate a representation of its internal state that can later be used
   * to create a new instance with that same state.
   *
   * @return Returns a Parcelable object containing the view's current dynamic state, or null if
   * there is nothing interesting to save.
   * @see #onRestoreInstanceState(Parcelable)
   * @see #saveHierarchyState(SparseArray)
   * @see #dispatchSaveInstanceState(SparseArray)
   * @see #setSaveEnabled(boolean)
   */
  @Override
  public Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("instanceState", super.onSaveInstanceState());
    bundle.putFloat("saveScale", normalisedScale);
    bundle.putFloat("matchViewHeight", matchViewHeight);
    bundle.putFloat("matchViewWidth", matchViewWidth);
    bundle.putInt("viewWidth", viewWidth);
    bundle.putInt("viewHeight", viewHeight);
    matrix.getValues(m);
    bundle.putFloatArray("matrix", m);
    bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce);
    return bundle;
  }

  /**
   * Hook allowing a view to re-apply a representation of its internal state that had previously
   * been generated by {@link #onSaveInstanceState}.
   *
   * @param state The frozen state that had previously been returned by {@link
   * #onSaveInstanceState}.
   * @see #onSaveInstanceState()
   * @see #restoreHierarchyState(android.util.SparseArray)
   * @see #dispatchRestoreInstanceState(android.util.SparseArray)
   */
  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      normalisedScale = bundle.getFloat("saveScale");
      m = bundle.getFloatArray("matrix");
      prevMatrix.setValues(m);
      prevMatchViewHeight = bundle.getFloat("matchViewHeight");
      prevMatchViewWidth = bundle.getFloat("matchViewWidth");
      prevViewHeight = bundle.getInt("viewHeight");
      prevViewWidth = bundle.getInt("viewWidth");
      imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered");
      super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
      return;
    }

    super.onRestoreInstanceState(state);
  }

  /**
   * Implemented to do the drawing
   *
   * @param canvas the canvas on which the background will be drawn
   */
  @Override
  protected void onDraw(Canvas canvas) {
    onDrawReady = true;
    imageRenderedAtLeastOnce = true;
    if (delayedZoomVariables != null) {
      setZoom(
          delayedZoomVariables.scale,
          delayedZoomVariables.focusX,
          delayedZoomVariables.focusY,
          delayedZoomVariables.scaleType
      );
      delayedZoomVariables = null;
    }
    super.onDraw(canvas);
  }

  /**
   * Called when the current configuration of the resources being used by the application have
   * changed.
   *
   * @param newConfig The new resource configuration.
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    // Save the values to persist the zooms state
    savePreviousImageValues();
  }

  /**
   * Get the current zoom. This is the zoom relative to the initial scale, not the original
   * resource.
   *
   * @return current zoom multiplier.
   */
  public float getCurrentZoom() {
    return normalisedScale;
  }

  /**
   * Reset zoom and translation to initial state.
   */
  public void resetZoom() {
    normalisedScale = 1;
    fitImageToView();
  }

  /**
   * Set the state of the zoom
   *
   * @param state The state of the zoom
   * @see State
   */
  private void setState(State state) {
    this.state = state;
  }

  /**
   * Set zoom to the specified scale. Image will be centered around the point (focusX, focusY).
   * These floats range from 0 to 1 and denote the focus point as a fraction from the left and top
   * of the view. For example, the top left corner of the image would be (0, 0). And the bottom
   * right corner would be (1, 1).
   */
  public void setZoom(float scale, float focusX, float focusY, ScaleType scaleType) {
    // setZoom can be called before the image is on the screen, but at this point,
    // image and view sizes have not yet been calculated in onMeasure. Thus, we should
    // delay calling setZoom until the view has been measured.
    if (!onDrawReady) {
      delayedZoomVariables = new ZoomVariables(scale, focusX, focusY, scaleType);
      return;
    }

    if (scaleType != mScaleType) {
      setScaleType(scaleType);
    }

    resetZoom();
    scaleImage(scale, viewWidth / 2, viewHeight / 2, true);
    matrix.getValues(m);
    m[Matrix.MTRANS_X] = -((focusX * getImageWidth()) - (viewWidth * 0.5f));
    m[Matrix.MTRANS_Y] = -((focusY * getImageHeight()) - (viewHeight * 0.5f));
    matrix.setValues(m);
    fixTrans();
    setImageMatrix(matrix);
  }

  /**
   * Set zoom parameters equal to another ZoomImageView. Including scale, position, and ScaleType.
   */
  public void setZoom(ZoomImageView img) {
    PointF center = img.getScrollPosition();
    setZoom(img.getCurrentZoom(), center.x, center.y, img.getScaleType());
  }

  /**
   * Return the point at the center of the zoomed image. The PointF coordinates range in value
   * between 0 and 1 and the focus point is denoted as a fraction from the left and top of the view.
   * For example, the top left corner of the image would be (0, 0). And the bottom right corner
   * would be (1, 1).
   *
   * @return PointF representing the scroll position of the zoomed image.
   */
  public PointF getScrollPosition() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return null;
    }
    final int drawableWidth = drawable.getIntrinsicWidth();
    final int drawableHeight = drawable.getIntrinsicHeight();

    PointF point = transformCoordTouchToBitmap(viewWidth / 2, viewHeight / 2, true);
    point.x /= drawableWidth;
    point.y /= drawableHeight;
    return point;
  }

  /**
   * Get the normalised width of {@link ZoomImageView}
   *
   * @return float The normalised width of the {@link ZoomImageView}
   */
  private float getImageWidth() {
    return matchViewWidth * normalisedScale;
  }

  /**
   * Get the normalised height of {@link ZoomImageView}
   *
   * @return float The normalised height of the {@link ZoomImageView}
   */
  private float getImageHeight() {
    return matchViewHeight * normalisedScale;
  }

  /**
   * Performs boundary checking and fixes the image matrix if it is out of bounds.
   */
  private void fixTrans() {
    matrix.getValues(m);
    float transX = m[Matrix.MTRANS_X];
    float transY = m[Matrix.MTRANS_Y];

    float fixTransX = getFixTrans(transX, viewWidth, getImageWidth());
    float fixTransY = getFixTrans(transY, viewHeight, getImageHeight());

    if (fixTransX != 0 || fixTransY != 0) {
      matrix.postTranslate(fixTransX, fixTransY);
    }
  }

  /**
   * When transitioning from zooming from focus to zoom from center (or vice versa) the image can
   * become unaligned within the view. This is apparent when zooming quickly. When the content size
   * is less than the view size, the content will often be centered incorrectly within the view.
   * fixScaleTrans first calls fixTrans() and then makes sure the image is centered correctly within
   * the view.
   */
  private void fixScaleTrans() {
    fixTrans();
    matrix.getValues(m);
    if (getImageWidth() < viewWidth) {
      m[Matrix.MTRANS_X] = (viewWidth - getImageWidth()) / 2;
    }

    if (getImageHeight() < viewHeight) {
      m[Matrix.MTRANS_Y] = (viewHeight - getImageHeight()) / 2;
    }
    matrix.setValues(m);
  }

  /**
   * @param trans The transition
   * @param viewSize The view size
   * @param contentSize The content size
   * @return float The fixed transition
   */
  private float getFixTrans(float trans, float viewSize, float contentSize) {
    float minTrans, maxTrans;

    if (contentSize <= viewSize) {
      minTrans = 0;
      maxTrans = viewSize - contentSize;

    } else {
      minTrans = viewSize - contentSize;
      maxTrans = 0;
    }

    if (trans < minTrans) {
      return -trans + minTrans;
    }
    if (trans > maxTrans) {
      return -trans + maxTrans;
    }
    return 0;
  }

  /**
   * @param delta The delta of the transition
   * @param viewSize The view size
   * @param contentSize The content size
   * @return float The delta of the transition
   */
  private float getFixDragTrans(float delta, float viewSize, float contentSize) {
    if (contentSize <= viewSize) {
      return 0;
    }
    return delta;
  }

  /**
   * Measure the view and its content to determine the measured width and the measured height.
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent. The
   * requirements are encoded with {@link android.view.View.MeasureSpec}.
   * @param heightMeasureSpec vertical space requirements as imposed by the parent. The requirements
   * are encoded with {@link android.view.View.MeasureSpec}.
   * @see #getMeasuredWidth()
   * @see #getMeasuredHeight()
   * @see #setMeasuredDimension(int, int)
   * @see #getSuggestedMinimumHeight()
   * @see #getSuggestedMinimumWidth()
   * @see android.view.View.MeasureSpec#getMode(int)
   * @see android.view.View.MeasureSpec#getSize(int)
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final Drawable drawable = getDrawable();

    if (drawable == null ||
        drawable.getIntrinsicWidth() == 0 ||
        drawable.getIntrinsicHeight() == 0
    ) {
      setMeasuredDimension(0, 0);
      return;
    }

    final int drawableWidth = drawable.getIntrinsicWidth();
    final int drawableHeight = drawable.getIntrinsicHeight();
    final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    viewWidth = setViewSize(widthMode, widthSize, drawableWidth);
    viewHeight = setViewSize(heightMode, heightSize, drawableHeight);

    // Set view dimensions, must be called otherwise IllegalStateException will be thrown
    setMeasuredDimension(viewWidth, viewHeight);

    // Fit content within view
    fitImageToView();
  }

  /**
   * If the normalisedScale is equal to 1, then the image is made to fit the screen. Otherwise, it
   * is made to fit the screen according to the dimensions of the previous image matrix. This allows
   * the image to maintain its zoom after rotation.
   */
  private void fitImageToView() {
    Drawable drawable = getDrawable();
    if (drawable == null || drawable.getIntrinsicWidth() == 0
        || drawable.getIntrinsicHeight() == 0) {
      return;
    }
    if (matrix == null || prevMatrix == null) {
      return;
    }

    int drawableWidth = drawable.getIntrinsicWidth();
    int drawableHeight = drawable.getIntrinsicHeight();

    // Scale image for view
    float scaleX = (float) viewWidth / drawableWidth;
    float scaleY = (float) viewHeight / drawableHeight;

    // All ZoomImageView will use FIT_CENTER scale type
    scaleX = scaleY = Math.min(scaleX, scaleY);

    // Center the image
    final float redundantXSpace = viewWidth - (scaleX * drawableWidth);
    final float redundantYSpace = viewHeight - (scaleY * drawableHeight);
    matchViewWidth = viewWidth - redundantXSpace;
    matchViewHeight = viewHeight - redundantYSpace;

    if (!isZoomed() && !imageRenderedAtLeastOnce) {
      // Stretch and center image to fit view
      matrix.setScale(scaleX, scaleY);
      matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
      normalisedScale = 1;

    } else {
      // These values should never be 0 or we will set viewWidth and viewHeight
      // to NaN in translateMatrixAfterRotate. To avoid this, call savePreviousImageValues
      // to set them equal to the current values.
      if (prevMatchViewWidth == 0 || prevMatchViewHeight == 0) {
        savePreviousImageValues();
      }

      prevMatrix.getValues(m);

      // Rescale Matrix after rotation
      m[Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalisedScale;
      m[Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalisedScale;

      // TransX and TransY from previous matrix
      float transX = m[Matrix.MTRANS_X];
      float transY = m[Matrix.MTRANS_Y];

      // Width
      float prevActualWidth = prevMatchViewWidth * normalisedScale;
      float actualWidth = getImageWidth();
      translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth,
          prevViewWidth, viewWidth, drawableWidth);

      // Height
      float prevActualHeight = prevMatchViewHeight * normalisedScale;
      float actualHeight = getImageHeight();
      translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight,
          prevViewHeight, viewHeight, drawableHeight);

      // Set the matrix to the adjusted scale and translate values.
      matrix.setValues(m);
    }

    fixTrans();
    setImageMatrix(matrix);
  }

  /**
   * Set view dimensions based on layout params
   *
   * @param mode The mode of {@link android.view.View.MeasureSpec}
   * @param size The view size
   * @param drawableWidth The width of the drawable
   * @return int The calculated view size
   */
  private int setViewSize(int mode, int size, int drawableWidth) {
    int viewSize;
    switch (mode) {
      case MeasureSpec.AT_MOST:
        viewSize = Math.min(drawableWidth, size);
        break;

      case MeasureSpec.UNSPECIFIED:
        viewSize = drawableWidth;
        break;

      default:
        viewSize = size;
        break;
    }
    return viewSize;
  }

  /**
   * After rotating, the matrix needs to be translated. This function finds the area of image which
   * was previously centered and adjusts translations so that is again the center, post-rotation.
   *
   * @param axis Matrix.MTRANS_X or Matrix.MTRANS_Y
   * @param trans the value of trans in that axis before the rotation
   * @param prevImageSize the width/height of the image before the rotation
   * @param imageSize width/height of the image after rotation
   * @param prevViewSize width/height of view before rotation
   * @param viewSize width/height of view after rotation
   * @param drawableSize width/height of drawable
   */
  private void translateMatrixAfterRotate(
      int axis,
      float trans,
      float prevImageSize,
      float imageSize,
      int prevViewSize,
      int viewSize,
      int drawableSize
  ) {
    if (imageSize < viewSize) {
      // The width/height of image is less than the view's width/height. Center it.
      m[axis] = (viewSize - (drawableSize * m[Matrix.MSCALE_X])) * 0.5f;

    } else if (trans > 0) {
      // The image is larger than the view, but was not before rotation. Center it.
      m[axis] = -((imageSize - viewSize) * 0.5f);

    } else {
      // Find the area of the image which was previously centered in the view. Determine its distance
      // from the left/top side of the view as a fraction of the entire image's width/height. Use that percentage
      // to calculate the trans in the new view width/height.
      float percentage = (Math.abs(trans) + (0.5f * prevViewSize)) / prevImageSize;
      m[axis] = -((percentage * imageSize) - (viewSize * 0.5f));
    }
  }

  /**
   * Check if this view can be scrolled horizontally in a certain direction.
   *
   * @param direction Negative to check scrolling left, positive to check scrolling right.
   * @return true if this view can be scrolled in the specified direction, false otherwise.
   */
  @Override
  public boolean canScrollHorizontally(int direction) {
    matrix.getValues(m);
    float x = m[Matrix.MTRANS_X];

    if (getImageWidth() < viewWidth) {
      return false;

    } else if (x >= -1 && direction < 0) {
      return false;

    } else {
      return !(Math.abs(x) + viewWidth + 1 >= getImageWidth()) || direction <= 0;
    }
  }

  /**
   * Gesture Listener detects a single click or long click and passes that on to the view's
   * listener.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class GestureListener extends GestureDetector.SimpleOnGestureListener {

    /**
     * Notified when a single-tap occurs
     *
     * @param e The down motion event of the single-tap.
     * @return boolean {@code true} if the event is consumed, else {@code false}
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      if (doubleTapListener != null) {
        return doubleTapListener.onSingleTapConfirmed(e);
      }
      return performClick();
    }

    /**
     * Notified when a fling occurs
     *
     * @param e1 The first down motion event that started the fling.
     * @param e2 The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second along the y axis.
     * @return boolean {@code true} if the event is consumed, else {@code false}
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      if (fling != null) {
        // If a previous fling is still active, it should be cancelled so that two flings
        // are not run simultaneously.
        fling.cancelFling();
      }
      fling = new Fling((int) velocityX, (int) velocityY);
      postOnAnimation(fling);

      return super.onFling(e1, e2, velocityX, velocityY);
    }

    /**
     * Notified when a double-tap occurs
     *
     * @param e The down motion event of the double-tap.
     * @return boolean {@code true} if the event is consumed, else {@code false}
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
      boolean consumed = false;
      if (doubleTapListener != null) {
        consumed = doubleTapListener.onDoubleTap(e);
      }
      if (state == State.NONE) {
        float targetZoom = (normalisedScale == MIN_SCALE) ? MAX_SCALE : MIN_SCALE;
        DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
        postOnAnimation(doubleTap);
        consumed = true;
      }
      return consumed;
    }

    /**
     * Notified when an event within a double-tap gesture occurs, including the down, move, and up
     * events.
     *
     * @param e The motion event that occurred during the double-tap gesture.
     * @return boolean {@code true} if the event is consumed, else {@code false}
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      if (doubleTapListener != null) {
        return doubleTapListener.onDoubleTapEvent(e);
      }
      return false;
    }
  }

  /**
   * An interface to handle events of {@link ZoomImageView}
   */
  public interface OnZoomImageViewListener {

    /**
     * Callback when the {@link ZoomImageView} has moved
     */
    void onMove();
  }

  /**
   * Responsible for all touch events. Handles the heavy lifting of drag and also sends touch events
   * to Scale Detector and Gesture Detector.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class PrivateOnTouchListener implements OnTouchListener {

    // Remember last point position for dragging
    private PointF last = new PointF();

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to get a chance to
     * respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     * @return boolean {@code true} if the listener has consumed the event, {@code false} otherwise
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      mScaleDetector.onTouchEvent(event);
      mGestureDetector.onTouchEvent(event);
      PointF curr = new PointF(event.getX(), event.getY());

      if (state == State.NONE || state == State.DRAG || state == State.FLING) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            last.set(curr);
            if (fling != null) {
              fling.cancelFling();
            }
            setState(State.DRAG);
            break;

          case MotionEvent.ACTION_MOVE:
            if (state == State.DRAG) {
              float deltaX = curr.x - last.x;
              float deltaY = curr.y - last.y;
              float fixTransX = getFixDragTrans(deltaX, viewWidth, getImageWidth());
              float fixTransY = getFixDragTrans(deltaY, viewHeight, getImageHeight());
              matrix.postTranslate(fixTransX, fixTransY);
              fixTrans();
              last.set(curr.x, curr.y);
            }
            break;

          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_POINTER_UP:
            setState(State.NONE);
            v.performClick();
            break;
        }
      }

      setImageMatrix(matrix);

      // User-defined OnTouchListener
      if (userTouchListener != null) {
        userTouchListener.onTouch(v, event);
      }

      // OnZoomImageViewListener is set: ZoomImageView dragged by user.
      if (zoomImageViewListener != null) {
        zoomImageViewListener.onMove();
      }

      // indicate event was consumed
      return true;
    }
  }

  /**
   * ScaleListener detects user two finger scaling and scales image.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    /**
     * Responds to the beginning of a scaling gesture. Reported by new pointers going down.
     *
     * @param detector The detector reporting the event - use this to retrieve extended info about
     * event state.
     * @return Whether or not the detector should continue recognizing this gesture.
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      setState(State.ZOOM);
      return true;
    }

    /**
     * Responds to scaling events for a gesture in progress. Reported by pointer motion.
     *
     * @param detector The detector reporting the event - use this to retrieve extended info about
     * event state.
     * @return Whether or not the detector should consider this event as handled.
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);

      // OnZoomImageViewListener is set: ZoomImageView pinch zoomed by user.
      if (zoomImageViewListener != null) {
        zoomImageViewListener.onMove();
      }
      return true;
    }

    /**
     * Responds to the end of a scale gesture. Reported by existing pointers going up.
     *
     * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()} and {@link
     * ScaleGestureDetector#getFocusY()} will return focal point of the pointers remaining on the
     * screen.
     *
     * @param detector The detector reporting the event - use this to retrieve extended info about
     * event state.
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      super.onScaleEnd(detector);

      setState(State.NONE);
      boolean animateToZoomBoundary = false;
      float targetZoom = normalisedScale;

      if (normalisedScale > MAX_SCALE) {
        targetZoom = MAX_SCALE;
        animateToZoomBoundary = true;

      } else if (normalisedScale < MIN_SCALE) {
        targetZoom = MIN_SCALE;
        animateToZoomBoundary = true;
      }

      if (animateToZoomBoundary) {
        DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, viewWidth / 2, viewHeight / 2,
            true);
        postOnAnimation(doubleTap);
      }
    }
  }

  /**
   * Scale the image based on the amount of zoom
   *
   * @param deltaScale The delta between the scales
   * @param focusX The X value of the focus
   * @param focusY The Y value of the focus
   * @param stretchImageToSuper {@code true} if the image is to be strected to super size, {@code
   * false} otherwise
   */
  private void scaleImage(double deltaScale, float focusX, float focusY,
      boolean stretchImageToSuper) {

    float lowerScale, upperScale;
    if (stretchImageToSuper) {
      lowerScale = superMinScale;
      upperScale = superMaxScale;

    } else {
      lowerScale = MIN_SCALE;
      upperScale = MAX_SCALE;
    }

    float origScale = normalisedScale;
    normalisedScale *= deltaScale;

    if (normalisedScale > upperScale) {
      normalisedScale = upperScale;
      deltaScale = upperScale / origScale;

    } else if (normalisedScale < lowerScale) {
      normalisedScale = lowerScale;
      deltaScale = lowerScale / origScale;
    }

    matrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
    fixScaleTrans();
  }

  /**
   * DoubleTapZoom calls a series of runnables which apply an animated zoom in/out graphic to the
   * image.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class DoubleTapZoom implements Runnable {

    // Time to bounce back when exceeding MIN or MAX zoom
    private static final float ZOOM_TIME = 200;

    private long startTime;
    private float startZoom, targetZoom;
    private float bitmapX, bitmapY;
    private boolean stretchImageToSuper;
    private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    private PointF startTouch;
    private PointF endTouch;

    /**
     * Constructor of {@link DoubleTapZoom}
     *
     * @param targetZoom The target of the zoom
     * @param focusX The X value of the focus (position tapped)
     * @param focusY The Y value of the focus (position tapped)
     * @param stretchImageToSuper {@code true} if the image is to be strected to super size, {@code
     * false} otherwise
     */
    DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
      setState(State.ANIMATE_ZOOM);
      startTime = System.currentTimeMillis();
      this.startZoom = normalisedScale;
      this.targetZoom = targetZoom;
      this.stretchImageToSuper = stretchImageToSuper;
      PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
      this.bitmapX = bitmapPoint.x;
      this.bitmapY = bitmapPoint.y;

      // Used for translating image during scaling
      startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
      endTouch = new PointF(viewWidth / 2, viewHeight / 2);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread,
     * starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing thread.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      float t = interpolate();
      double deltaScale = calculateDeltaScale(t);
      scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper);
      translateImageToCenterTouchPosition(t);
      fixScaleTrans();
      setImageMatrix(matrix);

      // OnZoomImageViewListener is set: double tap runnable updates listener
      // with every frame.
      if (zoomImageViewListener != null) {
        zoomImageViewListener.onMove();
      }

      if (t < 1f) {
        // Haven't finished zooming yet
        postOnAnimation(this);

      } else {
        // Finished zooming
        setState(State.NONE);
      }
    }

    /**
     * Interpolate between where the image should start and end in order to translate the image so
     * that the point that is touched is what ends up centered at the end of the zoom.
     */
    private void translateImageToCenterTouchPosition(float t) {
      float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
      float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
      PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
      matrix.postTranslate(targetX - curr.x, targetY - curr.y);
    }

    /**
     * Use interpolator to get time
     */
    private float interpolate() {
      long currTime = System.currentTimeMillis();
      float elapsed = (currTime - startTime) / ZOOM_TIME;
      elapsed = Math.min(1f, elapsed);
      return interpolator.getInterpolation(elapsed);
    }

    /**
     * Interpolate the current targeted zoom and get the delta from the current zoom.
     */
    private double calculateDeltaScale(float t) {
      double zoom = startZoom + t * (targetZoom - startZoom);
      return zoom / normalisedScale;
    }
  }

  /**
   * This function will transform the coordinates in the touch event to the coordinate system of the
   * drawable that the imageview contain
   *
   * @param x x-coordinate of touch event
   * @param y y-coordinate of touch event
   * @param clipToBitmap Touch event may occur within view, but outside image content. True, to clip
   * return value to the bounds of the bitmap size.
   * @return Coordinates of the point touched, in the coordinate system of the original drawable.
   */
  private PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
    matrix.getValues(m);
    final float origW = getDrawable().getIntrinsicWidth();
    final float origH = getDrawable().getIntrinsicHeight();
    final float transX = m[Matrix.MTRANS_X];
    final float transY = m[Matrix.MTRANS_Y];
    float finalX = ((x - transX) * origW) / getImageWidth();
    float finalY = ((y - transY) * origH) / getImageHeight();

    if (clipToBitmap) {
      finalX = Math.min(Math.max(finalX, 0), origW);
      finalY = Math.min(Math.max(finalY, 0), origH);
    }

    return new PointF(finalX, finalY);
  }

  /**
   * Inverse of transformCoordTouchToBitmap. This function will transform the coordinates in the
   * drawable's coordinate system to the view's coordinate system.
   *
   * @param bx x-coordinate in original bitmap coordinate system
   * @param by y-coordinate in original bitmap coordinate system
   * @return Coordinates of the point in the view's coordinate system.
   */
  private PointF transformCoordBitmapToTouch(float bx, float by) {
    matrix.getValues(m);
    final float origW = getDrawable().getIntrinsicWidth();
    final float origH = getDrawable().getIntrinsicHeight();
    final float px = bx / origW;
    final float py = by / origH;
    final float finalX = m[Matrix.MTRANS_X] + getImageWidth() * px;
    final float finalY = m[Matrix.MTRANS_Y] + getImageHeight() * py;
    return new PointF(finalX, finalY);
  }

  /**
   * Fling launches sequential runnables which apply the fling graphic to the image. The values for
   * the translation are interpolated by the Scroller.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class Fling implements Runnable {

    OverScroller scroller;
    int currX, currY;

    Fling(int velocityX, int velocityY) {
      setState(State.FLING);
      scroller = new OverScroller(context);
      matrix.getValues(m);

      int startX = (int) m[Matrix.MTRANS_X];
      int startY = (int) m[Matrix.MTRANS_Y];
      int minX, maxX, minY, maxY;

      if (getImageWidth() > viewWidth) {
        minX = viewWidth - (int) getImageWidth();
        maxX = 0;

      } else {
        minX = maxX = startX;
      }

      if (getImageHeight() > viewHeight) {
        minY = viewHeight - (int) getImageHeight();
        maxY = 0;

      } else {
        minY = maxY = startY;
      }

      scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
      currX = startX;
      currY = startY;
    }

    /**
     * Cancel the fling event
     */
    void cancelFling() {
      if (scroller != null) {
        setState(State.NONE);
        scroller.forceFinished(true);
      }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread,
     * starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing thread.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

      // OnZoomImageViewListener is set: ZoomImageView listener has been flung by user.
      // Listener runnable updated with each frame of fling animation.
      if (zoomImageViewListener != null) {
        zoomImageViewListener.onMove();
      }

      if (scroller.isFinished()) {
        scroller = null;
        return;
      }

      if (scroller.computeScrollOffset()) {
        int newX = scroller.getCurrX();
        int newY = scroller.getCurrY();
        int transX = newX - currX;
        int transY = newY - currY;
        currX = newX;
        currY = newY;
        matrix.postTranslate(transX, transY);
        fixTrans();
        setImageMatrix(matrix);
        postOnAnimation(this);
      }
    }
  }

  /**
   * A class to store the zoom variables
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  private class ZoomVariables {

    final float scale;
    final float focusX;
    final float focusY;
    public ScaleType scaleType;

    /**
     * Constructor for {@link ZoomVariables}
     *
     * @param scale Scale of the {@link ZoomImageView}
     * @param focusX X coordinate of the focus
     * @param focusY Y coordinate of the focus
     * @param scaleType Scale type of the {@link ZoomImageView}
     */
    ZoomVariables(float scale, float focusX, float focusY, ScaleType scaleType) {
      this.scale = scale;
      this.focusX = focusX;
      this.focusY = focusY;
      this.scaleType = scaleType;
    }
  }
}
