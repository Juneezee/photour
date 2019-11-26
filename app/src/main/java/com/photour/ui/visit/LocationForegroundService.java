package com.photour.ui.visit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.photour.MainActivity;
import com.photour.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.maps.model.LatLng;

public class LocationForegroundService extends Service {

  private static final String TAG = LocationForegroundService.class.getSimpleName();
  private static final String CHANNEL_ID = "photour";
  private static final String STARTED_FROM_NOTIFICATION = "com.photour.started_from_notification";

  // Constants for Google Map location request
  private static final int UPDATE_INTERVAL = 20000;
  private static final int FASTEST_INTERVAL = 1000;
  private static final float MIN_DISPLACEMENT = 5;

  private final IBinder binder = new LocalBinder();

  /**
   * Used to check whether the bound activity has really gone away and not unbound as part of an
   * orientation change. We create a foreground service notification only if the former takes
   * place.
   */
  private boolean changingConfiguration = false;

  private StartVisitMap visitMap;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private LocationCallback locationCallback;

  @Override
  public void onCreate() {
    super.onCreate();

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);

        Location location = locationResult.getLastLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        visitMap.latLngList.add(latLng);
        visitMap.currentLocation.setValue(location);
      }
    };

    createNotificationChannel();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "LocationForegroundService onStartCommand");
    boolean startedFromNotification = intent.getBooleanExtra(STARTED_FROM_NOTIFICATION, false);

    // We got here because the user decided to remove location updates from the notification.
    if (startedFromNotification) {
      removeLocationUpdates();
      stopSelf();
    }

    // Tells the system to not try to recreate the service after it has been killed.
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stopForeground(true);
    stopSelf();
    Log.d("LocationService", "onDestroy called");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    changingConfiguration = true;
  }

  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "In onBind()");
    stopForeground(true);
    return binder;
  }

  @Override
  public void onRebind(Intent intent) {
    Log.d(TAG, "In onRebind()");
    stopForeground(true);
    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.d(TAG, "Last client unbound from service");

    // Called when the last client (StartVisitFragment) unbinds from this
    // service. If this method is called due to a configuration change in StartVisitFragment, we
    // do nothing. Otherwise, we make this service a foreground service.
    if (!changingConfiguration) {
      Log.d(TAG, "Starting foreground service");

      startForeground(1, createNotification());
    }

    // Ensures onRebind() is called when a client re-binds.
    return true;
  }

  /**
   * Class used for the client Binder.  Since this service runs in the same process as its clients,
   * we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {

    LocationForegroundService getService() {
      return LocationForegroundService.this;
    }
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.app_name);
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
          NotificationManager.IMPORTANCE_LOW);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = (NotificationManager) getSystemService(
          NOTIFICATION_SERVICE);
      notificationManager.createNotificationChannel(channel);
    }
  }

  private Notification createNotification() {
    // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
    Bundle bundle = new Bundle();
    bundle.putBoolean(STARTED_FROM_NOTIFICATION, true);

//    PendingIntent pendingIntent = new NavDeepLinkBuilder(getApplicationContext()).setComponentName(
//        MainActivity.class).setGraph(R.navigation.navigation_visit).setDestination(R.id.start_visit)
//        .setArguments(bundle)
//        .createPendingIntent();

    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_visit)
        .setContentTitle("Ongoing visit")
        .setContentText("some trip")
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_LOW);

    return builder.build();
  }

  public void getLastLocation() {
    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
      if (task.isSuccessful() && task.getResult() != null) {
        visitMap.currentLocation.setValue(task.getResult());
      } else {
        Log.d(TAG, "Failed to get last location");
      }
    });
  }

  public void requestLocationUpdates(StartVisitFragment startVisitFragment) {
    this.visitMap = startVisitFragment.startVisitMap;

    Log.d(TAG, "Requesting location updates...");
    LocationRequest locationRequest = new LocationRequest()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(UPDATE_INTERVAL)
        .setFastestInterval(FASTEST_INTERVAL)
        .setMaxWaitTime(UPDATE_INTERVAL)
        .setSmallestDisplacement(MIN_DISPLACEMENT);

    startService(new Intent(getApplicationContext(), LocationForegroundService.class));

    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
        Looper.myLooper());
  }

  public void removeLocationUpdates() {
    Log.d(TAG, "Removed location updates");
    changingConfiguration = true;
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    stopSelf();
  }
}
