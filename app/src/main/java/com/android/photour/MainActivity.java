package com.android.photour;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.android.photour.ui.visit.StartVisitActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

/**
 * Main activity of the application
 *
 * @author Zer Jun Eng & Jia Hua Ng
 */
public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration appBarConfiguration;
  private DrawerLayout drawer;
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
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationView drawerView = findViewById(R.id.drawer_view);
    navView = findViewById(R.id.nav_view);
    drawer = findViewById(R.id.drawer_layout);
    appBarConfiguration = new AppBarConfiguration.Builder(
        R.id.navigation_visit, R.id.navigation_photos,
        R.id.navigation_paths).setDrawerLayout(drawer).build();

    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.

    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(navView, navController);
    NavigationUI.setupWithNavController(drawerView, navController);
  }

  /**
   * Allows the side navigation bar to slide in when the hamburger button is clicked
   *
   * @return boolean True if the hamburger button is clicked
   */
  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    return NavigationUI.navigateUp(navController, appBarConfiguration)
        || super.onSupportNavigateUp();
  }

  /**
   * function to lock drawer. Used in fragment onCreateView and onDestroyView DELETE IF NOT USED AT
   * END OF PROJECT
   *
   * @param enabled set if drawer is locked or not
   */
  public void setDrawerLocked(boolean enabled) {
    if (enabled) {
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    } else {
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
  }

  /**
   * Method to disable bottom navigation bar. Used in fragment onCreateView and onDestroyView
   *
   * @param visible True if the bottom navigation bar should not be visible
   */
  public void setNavigationVisibility(boolean visible) {
    if (navView.isShown() && !visible) {
      navView.setVisibility(View.GONE);
    } else if (!navView.isShown() && visible) {
      navView.setVisibility(View.VISIBLE);
    }
  }

  /**
   *
   *
   * @param view
   */
  public void startNewVisit(View view) {
    startActivity(new Intent(this, StartVisitActivity.class));
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
