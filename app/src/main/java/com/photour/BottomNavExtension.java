package com.photour;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.Objects;

/**
 * Manages the various graphs needed for a [BottomNavigationView]. Modified and converted to Java
 * from sample provided by Google https://github.com/android/architecture-components-samples
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class  BottomNavExtension extends BottomNavigationView {

  private String selectedItemTag;
  private String firstFragmentTag;
  private boolean isOnFirstFragment;

  public BottomNavExtension(@NonNull Context context) {
    super(context);
  }

  public BottomNavExtension(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BottomNavExtension(@NonNull Context context, @Nullable AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Manages the various graphs needed for BottomNavigationView.
   *
   * @param navGraphIds List of Ids of nav graphs
   * @param fragmentManager Fragment Manager of MainActivity
   * @param containerId Id of navHostFragment
   * @param intent Intent of MainActivity
   * @return
   */
  public LiveData<NavController> setupWithNavController(List<Integer> navGraphIds,
      FragmentManager fragmentManager, int containerId, Intent intent) {

    // Map of tags
    SparseArray<String> graphIdToTagMap = new SparseArray<>();
    // Result. Mutable live data with the selected controlled
    MutableLiveData<NavController> selectedNavController = new MutableLiveData<>();
    int firstFragmentGraphId = 0;

    // First create a NavHostFragment for each NavGraph ID
    for (int i = 0; i < navGraphIds.size(); i++) {
      String fragmentTag = getFragmentTag(i);

      // Find or create the Navigation host fragment
      NavHostFragment navHostFragment = obtainNavHostFragment(
          fragmentManager, fragmentTag, navGraphIds.get(i), containerId);

      // Obtain its id
      int graphId = navHostFragment.getNavController().getGraph().getId();
      if (i == 0) {
        firstFragmentGraphId = graphId;
      }

      // Save to the map
      graphIdToTagMap.append(graphId, fragmentTag);

      if (this.getSelectedItemId() == graphId) {
        selectedNavController.setValue(navHostFragment.getNavController());
        attachNavHostFragment(fragmentManager, navHostFragment, i == 0);
      } else {
        detachNavHostFragment(fragmentManager, navHostFragment);
      }
    }

    selectedItemTag = graphIdToTagMap.get(this.getSelectedItemId());
    firstFragmentTag = graphIdToTagMap.get(firstFragmentGraphId);
    isOnFirstFragment = selectedItemTag.equals(firstFragmentTag);

    // Listener when navigation is selected on bottomnavview
    this.setOnNavigationItemSelectedListener(item -> {
      if (fragmentManager.isStateSaved()) {
        return false;
      } else {
        String newlySelectedItemTag = graphIdToTagMap.get(item.getItemId());
        if (!selectedItemTag.equals(newlySelectedItemTag)) {
          fragmentManager.popBackStack(firstFragmentTag,
              FragmentManager.POP_BACK_STACK_INCLUSIVE);
          NavHostFragment selectedFragment =
              (NavHostFragment) fragmentManager.findFragmentByTag(newlySelectedItemTag);

          if (!firstFragmentTag.equals(newlySelectedItemTag)) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(R.anim.nav_default_enter_anim,
                R.anim.nav_default_exit_anim,
                R.anim.nav_default_pop_enter_anim,
                R.anim.nav_default_pop_exit_anim);
            assert selectedFragment != null;
            ft.attach(selectedFragment);
            ft.setPrimaryNavigationFragment(selectedFragment);
            for (int i = 0; i < graphIdToTagMap.size(); i++) {
              int key = graphIdToTagMap.keyAt(i);
              // get the object by the key.
              String fragmentTagIter = graphIdToTagMap.get(key);
              if (!fragmentTagIter.equals(newlySelectedItemTag)) {
                try {
                  ft.detach(
                      Objects.requireNonNull(fragmentManager.findFragmentByTag(firstFragmentTag)));
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
            ft.addToBackStack(firstFragmentTag);
            ft.setReorderingAllowed(true);
            ft.commit();
          }
          selectedItemTag = newlySelectedItemTag;
          isOnFirstFragment = (selectedItemTag.equals(firstFragmentTag));
          assert selectedFragment != null;
          selectedNavController.setValue(selectedFragment.getNavController());
          return true;
        } else {
          return false;
        }
      }
    });

    this.setOnNavigationItemReselectedListener(item -> {
      String newlySelectedItemTag = graphIdToTagMap.get(item.getItemId());
      NavHostFragment selectedFragment =
          (NavHostFragment) fragmentManager.findFragmentByTag(newlySelectedItemTag);

      NavController navController;
      if (selectedFragment != null) {
        navController = selectedFragment.getNavController();
        navController.popBackStack(navController.getGraph().getStartDestination(), false);
      }
    });

    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent);

    int finalFirstFragmentGraphId = firstFragmentGraphId;

    fragmentManager.addOnBackStackChangedListener(() -> {
      int backStackCount = fragmentManager.getBackStackEntryCount();
      boolean isOnBackStack = false;
      for (int i = 0; i < backStackCount; i++) {
        if (Objects.equals(fragmentManager.getBackStackEntryAt(i).getName(), firstFragmentTag)) {
          isOnBackStack = true;
        }
      }
      if (!isOnFirstFragment && !isOnBackStack) {
        listenerSetSelectedItemId(finalFirstFragmentGraphId);
      }
      if (Objects.requireNonNull(selectedNavController.getValue()).getCurrentDestination()
          == null) {
        selectedNavController.getValue().navigate(
            selectedNavController.getValue().getGraph().getId()
        );
      }

    });
    return selectedNavController;
  }

  /**
   * Allow BottomNavigationView to able to handle Deep Links
   *
   * @param navGraphIds List of Ids of nav graphs
   * @param fragmentManager Fragment Manager of MainActivity
   * @param containerId Id of navHostFragment
   * @param intent Intent of MainActivity
   */
  private void setupDeepLinks(
      List<Integer> navGraphIds,
      FragmentManager fragmentManager,
      int containerId,
      Intent intent
  ) {
    for (int i = 0; i < navGraphIds.size(); i++) {
      String fragmentTag = getFragmentTag(i);
      // Find or create the Navigation host fragment
      NavHostFragment navHostFragment = obtainNavHostFragment(fragmentManager,
          fragmentTag, navGraphIds.get(i), containerId);

      if (navHostFragment.getNavController().handleDeepLink(intent)
          && this.getSelectedItemId() != navHostFragment.getNavController().getGraph().getId()) {
        this.setSelectedItemId(navHostFragment.getNavController().getGraph().getId());
      }
    }
  }

  /**
   * Set the id of currently selected item
   * @param itemId id of the selected item
   */
  private void listenerSetSelectedItemId(int itemId) {
    this.setSelectedItemId(itemId);
  }

  /**
   * Detach the navHostFragment from fragmentManager
   * @param fragmentManager FragmentManager of Mainactivity
   * @param navHostFragment NavHostFragment to be detached
   */
  private void detachNavHostFragment(
      FragmentManager fragmentManager,
      NavHostFragment navHostFragment
  ) {
    fragmentManager.beginTransaction().detach(navHostFragment).commitNow();
  }

  /**
   * Attach the navHostFragment from fragmentManager
   * @param fragmentManager FragmentManager of Mainactivity
   * @param navHostFragment NavHostFragment to be attached
   * @param isPrimaryNavFragment Boolean if the given fragment is the first fragment
   */
  private void attachNavHostFragment(
      FragmentManager fragmentManager,
      NavHostFragment navHostFragment,
      Boolean isPrimaryNavFragment
  ) {
    FragmentTransaction ft = fragmentManager.beginTransaction();
    ft.attach(navHostFragment);
    if (isPrimaryNavFragment) {
      ft.setPrimaryNavigationFragment(navHostFragment);
    }
    ft.commitNow();
  }

  /**
   *
   * @param fragmentManager
   * @param fragmentTag
   * @param navGraphId
   * @param containerId
   * @return
   */
  private NavHostFragment obtainNavHostFragment(
      FragmentManager fragmentManager,
      String fragmentTag,
      int navGraphId,
      int containerId
  ) {
    NavHostFragment existingFragment =
        (NavHostFragment) fragmentManager.findFragmentByTag(fragmentTag);
    if (existingFragment == null) {
      // If fragment doesn't exist, create it and return it.
      NavHostFragment navHostFragment = NavHostFragment.create(navGraphId);
      fragmentManager.beginTransaction()
          .add(containerId, navHostFragment, fragmentTag).commitNow();
      return navHostFragment;
    } else {
      // If the Nav Host fragment exists, return it
      return existingFragment;
    }
  }

  /**
   * Getter for tag of fragment
   * @param index current selected tab of bottomNavigationView
   * @return String tag of fragment
   */
  private String getFragmentTag(int index) {
    return "fragment#" + index;
  }
}

