package com.android.photour;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import static android.media.CamcorderProfile.get;

/**
 * Main activity of the application
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class MainActivity extends AppCompatActivity {

  private LiveData<NavController> currentNavController;
  private AppBarConfiguration appBarConfiguration;
  private BottomNavExtension navView;
  private Toolbar toolbar;
  private BottomNavigationView navView;

  /**
   * Perform the required actions when the activity is created
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   * down then this Bundle contains the data it most recently supplied in
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (savedInstanceState == null) {
      setupBottomNavigationBar();
    }
  }

  /**
   * This method is called after {@link #onStart} when the activity is being re-initialized from a
   * previously saved state. Performs a restore of any view state that had previously been frozen by
   * {@link #onSaveInstanceState}.
   *
   * @param savedInstanceState The data most recently supplied in {@link #onSaveInstanceState}.
   */
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    setupBottomNavigationBar();
  }

  /**
   * Called on first creation and when restoring state.
   *
   * Setups the bottomnavbar as well as the navcontroller for the fragment
   */
  private void setupBottomNavigationBar() {
    navView = findViewById(R.id.nav_view);
    List<Integer> navGraphIds = new ArrayList<>();
    navGraphIds.add(R.navigation.visits_navigation);
    navGraphIds.add(R.navigation.photos_navigation);
    navGraphIds.add(R.navigation.paths_navigation);
    LiveData<NavController> controller;

    controller = navView.setupWithNavController(
            navGraphIds,
            this.getSupportFragmentManager(),
            R.id.nav_host_fragment,
            this.getIntent()
    );

    final Observer<NavController> navControllerObserver = navController -> {
      navController.addOnDestinationChangedListener((controller1, destination, arguments) -> {
        switch (destination.getId()) {
          case (R.id.navigation_settings):
          case (R.id.navigation_activities):
          case (R.id.navigation_about):
            navView.setVisibility(View.GONE);
            break;
          default:
            navView.setVisibility(View.VISIBLE);
            break;
        }
      });
      NavigationUI.setupActionBarWithNavController(this, navController);
    };

    controller.observe(this, navControllerObserver);
    currentNavController = controller;
  }

//  /**
//   * Allows hamburger and back button to function
//   *
//   * @return if in menu or not
//   */
//  @Override
//  public boolean onSupportNavigateUp() {
//    return NavigationUI.navigateUp(Objects.requireNonNull(currentNavController.getValue()), appBarConfiguration)
//        || super.onSupportNavigateUp();
//  }

  /**
   * Initialize the contents of the Activity's standard options menu.
   *
   * @param menu The options menu in which you place your items.
   * @return boolean Must return true for the menu to be displayed; if return false it will not be
   * shown.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return true;
  }

  /**
   * Sets the visibility of top toolbar (Action bar)
   *
   * @param visible True if the top toolbar (Action bar) should be visible
   */
  public void setToolbarVisibility(boolean visible) {
    toolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  /**
   * Called to process touch screen event. Overridden for custom event handling
   *
   * @param ev The touch screen event
   * @return boolean Return true if this event was consumed.
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      View v = getCurrentFocus();

      // Clear focus of TextInputEditText and hide keyboard when tapped outside
      if (v instanceof TextInputEditText) {
        Rect outRect = new Rect();
        v.getGlobalVisibleRect(outRect);
        if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
          v.clearFocus();
          InputMethodManager imm = (InputMethodManager) getSystemService(
              Context.INPUT_METHOD_SERVICE);
          assert imm != null;
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
      }
    }

    return super.dispatchTouchEvent(ev);
  }
}
