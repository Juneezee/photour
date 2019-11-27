package com.photour.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * A helper class for {@link BroadcastReceiver}
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class ReceiverHelper {

  /**
   * Register broadcast receiver
   *
   * @param context The context of the current application
   * @param receiver The BroadcastReceiver to handle the broadcast.
   * @param action The action to match, such as Intent.ACTION_MAIN.
   */
  public static void registerBroadcastReceiver(
      Context context,
      BroadcastReceiver receiver,
      String action
  ) {
    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(action));
  }

  /**
   * Unregister broadcast receiver
   *
   * @param context The context of the current application
   * @param receiver The BroadcastReceiver to handle the broadcast.
   */
  public static void unregisterBroadcastReceiver(Context context, BroadcastReceiver receiver) {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
  }
}
