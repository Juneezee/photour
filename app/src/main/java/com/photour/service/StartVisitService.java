package com.photour.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.photour.BuildConfig;
import com.photour.MainActivity;
import com.photour.R;

public class StartVisitService extends JobService {

  private static final String TAG = StartVisitService.class.getSimpleName();
  public static final String ACTION_BROADCAST = BuildConfig.APPLICATION_ID + ".broadcast";
  public static final String EXTRA_LOCATION = BuildConfig.APPLICATION_ID + ".location";
  private static final String CHANNEL_ID = "photour";
  public static final int JOB_ID = 123;

  // Constants for Google Map location request
  private static final int UPDATE_INTERVAL = 20000;
  private static final int FASTEST_INTERVAL = 1000;
  private static final float MIN_DISPLACEMENT = 5;

  private FusedLocationProviderClient fusedLocationProviderClient;
  private LocationCallback locationCallback;

  /**
   * Called by the system when the service is first created.  Do not call this method directly.
   */
  @Override
  public void onCreate() {
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
   * Called to indicate that the job has begun executing.
   *
   * @param params Parameters specifying info about this job, including the optional extras
   * configured with {@link JobInfo.Builder#setExtras(android.os.PersistableBundle). This object
   * serves to identify this specific running job instance when calling {@link
   * #jobFinished(JobParameters, boolean)}.
   * @return {@code true} if service will continue running, using a separate thread when
   * appropriate. {@code false} means that this job has completed its work.
   */
  @Override
  public boolean onStartJob(JobParameters params) {
    System.out.println("Job is starting");
    startForeground(1, createNotification(params.getExtras().getString("title")));
    requestLocationUpdates();
    return true;
  }

  /**
   * This method is called if the system has determined that you must stop execution of your job
   * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
   *
   * @param params The parameters identifying this job, as supplied to the job in the {@link
   * #onStartJob(JobParameters)} callback.
   * @return {@code true} to indicate to the JobManager whether you'd like to reschedule this job
   * based on the retry criteria provided at job creation-time; or {@code false} to end the job
   * entirely.  Regardless of the value returned, your job must stop executing.
   */
  @Override
  public boolean onStopJob(JobParameters params) {
    System.out.println("Job is canceled");
    stopForeground(true);

    removeLocationUpdates();

    return true;
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
      notificationManager.createNotificationChannel(channel);
    }
  }

  private Notification createNotification(String newVisitTitle) {
//    PendingIntent pendingIntent = new NavDeepLinkBuilder(getApplicationContext()).setComponentName(
//        MainActivity.class).setGraph(R.navigation.navigation_visit).setDestination(R.id.start_visit)
//        .setArguments(bundle)
//        .createPendingIntent();

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

  private void onLocationChanged(Location location) {
    // Notify anyone listening for broadcasts about the new location.
    Intent intent = new Intent(ACTION_BROADCAST);
    intent.putExtra(EXTRA_LOCATION, location);
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
  }

  public void getLastLocation() {
    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
      if (task.isSuccessful() && task.getResult() != null) {
        onLocationChanged(task.getResult());
      } else {
        Log.d(TAG, "Failed to get last location");
      }
    });
  }

  private void requestLocationUpdates() {
    System.out.println("Requesting location updates");
    LocationRequest locationRequest = new LocationRequest()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(UPDATE_INTERVAL)
        .setFastestInterval(FASTEST_INTERVAL)
        .setMaxWaitTime(UPDATE_INTERVAL)
        .setSmallestDisplacement(MIN_DISPLACEMENT);

    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
        Looper.myLooper());
  }

  private void removeLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }
}
