package com.android.photour.ui.visit;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.android.photour.R;
import com.google.android.gms.location.LocationResult;

public class LocationIntentService extends IntentService {

  public static final String ACTION_PROCESS_UPDATES = "com.android.photour" + ".PROCESS_UPDATES";

  /**
   * Creates an IntentService.  Invoked by subclass's constructor.
   */
  public LocationIntentService() {
    super("LocationIntentService");
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
    Log.d("LocationIntentService", "onHandleIntent called");

    startForeground(1, getNotification());

    if (intent != null) {
      if (ACTION_PROCESS_UPDATES.equals(intent.getAction())) {

        if (LocationResult.hasResult(intent)) {
          LocationResult locationResult = LocationResult.extractResult(intent);

          if (locationResult != null) {
            locationResult.getLastLocation();

          }

        }
      }
    }
  }

  private Notification getNotification() {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "photour")
        .setSmallIcon(R.drawable.ic_visit)
        .setContentTitle("Ongoing visit")
        .setContentText("new visit title")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    return builder.build();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stopForeground(false);
    Log.d("LocationIntentService", "onDestroy called");
  }
}
