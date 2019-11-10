package com.android.photour;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
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

  private  LiveData<NavController> currentNavController = null;
  private AppBarConfiguration appBarConfiguration;
  private DrawerLayout drawer;
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
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    drawer = findViewById(R.id.drawer_layout);
    drawerView = findViewById(R.id.drawer_view);

    if (savedInstanceState == null) {
      setupBottomNavigationBar();
    }
  }

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

    final Observer<NavController> navControllerObserver =
            navController -> {
              navController.addOnDestinationChangedListener((controller1, destination, arguments) -> {
                switch (destination.getId()) {
                  case(R.id.navigation_settings):
                  case(R.id.navigation_activities):
                  case(R.id.navigation_about):
                    navView.setVisibility(View.GONE);
                    break;
                  default:
                    navView.setVisibility(View.VISIBLE);
                    break;
                }
              });
              NavigationUI.setupWithNavController(drawerView,navController);
              NavigationUI.setupActionBarWithNavController(this,navController,drawer);
    };

    controller.observe(this,navControllerObserver);
    currentNavController = controller;
  }

  /**
   * Allows hamburger and back button to function
   *
   * @return if in menu or not
   */
  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = currentNavController.getValue();
    return NavigationUI.navigateUp(navController,drawer)
            || super.onSupportNavigateUp();


  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }

  /**
   * Added to allow physical back button to function with drawer
   */
  @Override
  public void onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  /**
   * Sets the visibility of bottom navigation bar
   *
   * @param visible True if the bottom navigation bar should be visible
   */
  public void setNavigationVisibility(boolean visible) {
    if (navView.isShown() && !visible) {
      navView.setVisibility(View.GONE);
    } else if (!navView.isShown() && visible) {
      navView.setVisibility(View.VISIBLE);
    }
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
