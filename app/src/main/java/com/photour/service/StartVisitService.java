package com.photour.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.maps.model.LatLng;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.helper.LocationHelper;
import com.photour.ui.visitnew.ImageMarker;
import com.photour.ui.visitnew.StartVisitFragment;
import com.photour.ui.visitnew.StartVisitMap;
import java.util.ArrayList;

/**
 * Background JobService for tracking a new visit
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class StartVisitService extends Service {

  private static final String TAG = StartVisitService.class.getSimpleName();
  private static final String CHANNEL_ID = "photour";

  // Constants for Google Map location request
  private static final int UPDATE_INTERVAL = 20000;
  private static final int FASTEST_INTERVAL = 1000;
  private static final float MIN_DISPLACEMENT = 5;

  public static boolean isRunning = false;

  // Boolean to check if the current visit has been inserted into the database
  public boolean isVisitInserted = false;

  // Title of the new visit
  public String newVisitTitle;

  // The base time of the chronometer when the visit is started
  public long chronometerBase = SystemClock.elapsedRealtime();

  private FusedLocationProviderClient fusedLocationProviderClient;
  private LocationCallback locationCallback;

  private StartVisitMap visitMap;
  public final ArrayList<LatLng> latLngList = new ArrayList<>();
  public final ArrayList<ImageMarker> markerList = new ArrayList<>();

  private final IBinder binder = new LocalBinder();

  /**
   * Class used for the client Binder.  Since this service runs in the same process as its clients,
   * we don't need to deal with IPC.
   *
   * @author Zer Jun Eng, Jia Hua Ng
   */
  public class LocalBinder extends Binder {

    public StartVisitService getService() {
      return StartVisitService.this;
    }
  }

  /**
   * Called by the system when the service is first created.  Do not call this method directly.
   */
  @Override
  public void onCreate() {
    Log.d(TAG, "onCreating service...");
    super.onCreate();

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        onLocationChanged(locationResult.getLastLocation());
      }
    };

    createNotificationChannel();
  }

  /**
   * Called by the system every time a client explicitly starts the service by calling {@link
   * android.content.Context#startService}, providing the arguments it supplied and a unique integer
   * token representing the start request.
   *
   * @param intent The Intent supplied to {@link android.content.Context#startService}, as given.
   * This may be null if the service is being restarted after its process has gone away, and it had
   * previously returned anything except {@link #START_STICKY_COMPATIBILITY}.
   * @param flags Additional data about this start request.
   * @param startId A unique integer representing this specific request to start.  Use with {@link
   * #stopSelfResult(int)}.
   * @return The return value indicates what semantics the system should use for the service's
   * current started state.  It may be one of the constants associated with the {@link
   * #START_CONTINUATION_MASK} bits.
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");
    return START_NOT_STICKY;
  }

  /**
   * Called by the system to notify a Service that it is no longer used and is being removed.  The
   * service should clean up any resources it holds (threads, registered receivers, etc) at this
   * point.  Upon return, there will be no more calls in to this Service object and it is
   * effectively dead.
   */
  @Override
  public void onDestroy() {
    Log.d(TAG, "Stopping service...");
    super.onDestroy();
    isRunning = false;
    removeLocationUpdates();
    stopForeground(true);
    stopSelf();
  }

  /**
   * Return the communication channel to the service.  May return null if clients can not bind to
   * the service.
   *
   * @param intent The Intent that was used to bind to this service, as given to {@link
   * android.content.Context#bindService Context.bindService}
   * @return Return an IBinder through which clients can call on to the service.
   */
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "In onBind()");
    return binder;
  }

  /**
   * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is
   * new and not in the support library
   */
  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.app_name);
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
          NotificationManager.IMPORTANCE_LOW);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = (NotificationManager) getSystemService(
          NOTIFICATION_SERVICE);

      if (notificationManager != null) {
        notificationManager.createNotificationChannel(channel);
      }
    }
  }

  /**
   * Create a notification to indicate that a visit is ongoing
   *
   * @param newVisitTitle The title of the new visit
   * @return Notification A {@link Notification} object
   */
  private Notification createNotification(String newVisitTitle) {
    // Bring the application to front if the activity is in the background (not killed)
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_visit)
        .setSubText("Ongoing visit")
        .setContentTitle(newVisitTitle)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_LOW);

    return builder.build();
  }

  /**
   * Start this foreground service
   *
   * @param startVisitFragment An instance of the current {@link StartVisitFragment}
   */
  public void startService(StartVisitFragment startVisitFragment) {
    this.visitMap = startVisitFragment.startVisitMap;

    Log.d(TAG, "Starting service...");
    startService(new Intent(getApplicationContext(), StartVisitService.class));

    if (!isRunning()) {
      Log.d(TAG, "Starting foreground notification...");
      startForeground(1, createNotification(newVisitTitle));
    }

    isRunning = true;

    if (!isVisitInserted) {
      startVisitFragment.viewModel.insertVisit();
      isVisitInserted = true;
    }

    requestLocationUpdates();
  }

  /**
   * Check if this foreground service is already running
   *
   * @return boolean {@code true} if this foreground service is already running
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Notify anyone listening for broadcasts about the new location.
   *
   * @param location The last location
   */
  private void onLocationChanged(Location location) {
    Log.d(TAG, "Location changed");
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    if (LocationHelper.shouldAddToLatLntList(latLngList, latLng)) {
      latLngList.add(latLng);
    }

    visitMap.currentLocation.setValue(location);
  }

  /**
   * Get the last location of the device
   */
  public void getLastLocation() {
    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
      if (task.isSuccessful() && task.getResult() != null) {
        visitMap.currentLocation.setValue(task.getResult());
      } else {
        Log.d(TAG, "Failed to get last location");
      }
    });
  }

  /**
   * Request location update
   */
  public void requestLocationUpdates() {
    Log.d(TAG, "Requesting location updates");

    LocationRequest locationRequest = new LocationRequest()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(UPDATE_INTERVAL)
        .setFastestInterval(FASTEST_INTERVAL)
        .setMaxWaitTime(UPDATE_INTERVAL)
        .setSmallestDisplacement(MIN_DISPLACEMENT);

    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
        Looper.myLooper());
  }

  /**
   * Remove location updates
   */
  private void removeLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }
}
