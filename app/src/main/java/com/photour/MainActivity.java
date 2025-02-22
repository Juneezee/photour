package com.photour;

import android.content.Context;
import android.content.Intent;
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
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.libraries.maps.MapView;
import com.google.android.material.textfield.TextInputEditText;
import com.photour.helper.CacheHelper;
import com.photour.helper.PermissionHelper;
import com.photour.helper.PreferenceHelper;
import com.photour.service.StartVisitService;
import com.photour.ui.settings.SettingsFragmentDirections;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity of the application
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class MainActivity extends AppCompatActivity
    implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  private LiveData<NavController> currentNavController;
  private AppBarConfiguration appBarConfiguration;
  private BottomNavExtension navView;
  private Toolbar toolbar;
  public CacheHelper cacheHelper;

  /**
   * Perform the required actions when the activity is created
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   * down then this Bundle contains the data it most recently supplied in
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    PreferenceHelper.initializePreferences(getApplicationContext());
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    cacheHelper = new CacheHelper(this);

    // Bottom navigation bar only needs to be setup once
    if (savedInstanceState == null) {
      setupBottomNavigationBar();
    }

    preloadPlayServices();

    // Restore ongoing visit if the foreground service is running and state is persisted
    if (PermissionHelper.hasLocationPermission(this) && currentNavController != null
        && currentNavController.getValue() != null && StartVisitService.isRunning
    ) {
      restoreOngoingVisit(currentNavController.getValue());
    } else if (!StartVisitService.isRunning) {
      stopService(new Intent(getApplicationContext(), StartVisitService.class));
    }
  }

  /**
   * A visit is ongoing when the application was killed. Restore to {@link
   * com.photour.ui.visitnew.StartVisitFragment}
   *
   * @param controller A {@link NavController}
   */
  private void restoreOngoingVisit(@NonNull NavController controller) {
    NavDestination currentDestination = controller.getCurrentDestination();

    if (currentDestination != null && currentDestination.getId() == R.id.new_visit) {
      controller.navigate(R.id.start_visit);
    }
  }

  /**
   * Preload Google Play Services (client version and package version download)
   *
   * @see <a href="https://stackoverflow.com/a/29246677/7902371"></a>
   */
  private void preloadPlayServices() {
    new Thread(() -> {
      try {
        MapView mv = new MapView(getApplicationContext());
        mv.onCreate(null);
        mv.onPause();
        mv.onDestroy();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }

  /**
   * Called on first creation and when restoring state.
   *
   * Setups the bottomnavbar as well as the navcontroller for the fragment
   */
  private void setupBottomNavigationBar() {
    runOnUiThread(() -> {
      navView = findViewById(R.id.nav_view);
      List<Integer> navGraphIds = new ArrayList<>();
      navGraphIds.add(R.navigation.navigation_visit);
      navGraphIds.add(R.navigation.navigation_photos);
      navGraphIds.add(R.navigation.navigation_visits);

      LiveData<NavController> controller = navView.setupWithNavController(
          navGraphIds,
          this.getSupportFragmentManager(),
          R.id.nav_host_fragment,
          this.getIntent()
      );

      controller.observe(this, navController -> {
        navController.addOnDestinationChangedListener((controller1, destination, arguments) -> {
          switch (destination.getId()) {
            case R.id.new_visit:
            case R.id.photos:
            case R.id.visits:
              navView.setVisibility(View.VISIBLE);
              break;
            default:
              navView.setVisibility(View.GONE);
          }
        });

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
      });
      currentNavController = controller;
    });

  }

  /**
   * This method is called after {@link #onStart} when the activity is being re-initialized from a
   * previously saved state. Performs a restore of any view state that had previously been frozen by
   * {@link #onSaveInstanceState}.
   *
   * @param savedInstanceState The data most recently supplied in {@link #onSaveInstanceState}.
   */
  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    setupBottomNavigationBar();
  }

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

  /**
   * This hook is called whenever an item in your options menu is selected.
   *
   * @param item The menu item that was selected
   * @return boolean Return false to allow normal menu processing to proceed, true to consume it
   * here.
   */
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    /*
      Have the NavigationUI look for an action or destination matching the menu
      item id and navigate there if found.
      Otherwise, bubble up to the parent.
     */
    return NavigationUI.onNavDestinationSelected(item,
        Navigation.findNavController(this, R.id.nav_host_fragment))
        || super.onOptionsItemSelected(item);
  }

  /**
   * This method is called whenever the user chooses to navigate Up within the application's
   * activity hierarchy from the action bar.
   *
   * @return boolean True if Up navigation completed successfully and this Activity was finished,
   * false otherwise.
   */
  @Override
  public boolean onSupportNavigateUp() {
    // Have NavigationUI handle up behavior in the ActionBar
    return currentNavController.getValue() != null
        && (NavigationUI.navigateUp(currentNavController.getValue(), appBarConfiguration)
        || super.onSupportNavigateUp());
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
   * Set the title of the top toolbar (Action bar)
   *
   * @param title The title of the top toolbar (action bar)
   */
  public void setToolbarTitle(String title) {
    toolbar.setTitle(title);
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

  /**
   * Handles fragment calling in preference(Setting)
   * automatically links to About page as it is the only page
   *
   * @param caller Caller object to get fragment
   * @param pref Preference object that is calling the fragment
   * @return True if the call is completed, else False
   */
  @Override
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    if (pref.getFragment().equals("com.photour.ui.about.AboutFragment")
        && currentNavController.getValue() != null
    ) {
      currentNavController.getValue()
          .navigate(SettingsFragmentDirections.actionActionSettingsToAbout());
    }
    return true;
  }
}

