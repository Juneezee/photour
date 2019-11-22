package com.android.photour.ui.visit;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

public class LocationService extends IntentService {

  /**
   * Creates an IntentService.  Invoked by subclass's constructor.
   *
   * @param name Used to name the worker thread, important only for debugging.
   */
  public LocationService(String name) {
    super(name);
  }

  /**
   * This method is invoked on the worker thread with a request to process. Only one Intent is
   * processed at a time, but the processing happens on a worker thread that runs independently from
   * other application logic. So, if this code takes a long time, it will hold up other requests to
   * the same IntentService, but it will not hold up anything else. When all requests have been
   * handled, the IntentService stops itself, so you should not call {@link #stopSelf}.
   *
   * @param intent The value passed to {@link android.content.Context#startService(Intent)}. This
   * may be null if the service is being restarted after its process has gone away; see {@link
   * android.app.Service#onStartCommand} for details.
   */
  @Override
  protected void onHandleIntent(@Nullable Intent intent) {

  }
}
