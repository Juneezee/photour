package com.android.photour.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class Barometer {

  private static final String TAG = Barometer.class.getSimpleName();
  private long mSamplingRateInMSecs;
  private long mSamplingRateNano;
  private SensorEventListener mPressureListener = null;
  private SensorManager mSensorManager;
  private Sensor mBarometerSensor;
  private long timePhoneWasLastRebooted;
  private long BAROMETER_READING_FREQUENCY = 30000;
  private long lastReportTime = 0;
  private boolean started;
  private Accelerometer accelerometer;
  /**
   * this is used to stop the barometer if we have not seen any movement in the last 20 seconds
   */
  private static final long STOPPING_THRESHOLD = (long) 20000;


  public Barometer(Context context) {
    // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
    timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime();

    mSamplingRateNano = (long) (BAROMETER_READING_FREQUENCY) * 1000000;
    mSamplingRateInMSecs = (long) BAROMETER_READING_FREQUENCY;
    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mBarometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    initBarometerListener();
  }

  /**
   * it inits the listener and establishes the actions to take when a reading is available
   */
  private void initBarometerListener() {
    if (standardPressureSensorAvailable()) {
      mPressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          long diff = event.timestamp - lastReportTime;
          // time is in nanoseconds it represents the set reference times the first time we come here
          // set event timestamp to current time in milliseconds
          // see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
          // the following operation avoids reporting too many events too quickly - the sensor may always
          // misbehave and start sending data very quickly
          if (diff >= mSamplingRateNano) {
            long actualTimeInMseconds =
                timePhoneWasLastRebooted + (long) (event.timestamp / 1000000.0);
            float pressureValue = event.values[0];
            int accuracy = event.accuracy;

            lastReportTime = event.timestamp;
            // if we have not see any movement on the side of the accelerometer, let's stop
            long timeLag = actualTimeInMseconds - accelerometer.getLastReportTime();
            if (timeLag > STOPPING_THRESHOLD) {
              stopBarometer();
            }
          }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };
    }

  }


  /**
   * it returns true if the sensor is available
   */
  public boolean standardPressureSensorAvailable() {
    return (mBarometerSensor != null);
  }

  /**
   * it starts the pressure monitoring
   */
  public void startSensingPressure(Accelerometer accelerometer) {
    this.accelerometer = accelerometer;
    // if the sensor is null,then mSensorManager is null and we get a crash
    if (standardPressureSensorAvailable()) {
      // delay is in microseconds (1millisecond=1000 microseconds)
      // it does not seem to work though
      //stopBarometer();
      // otherwise we stop immediately because
      mSensorManager.registerListener(mPressureListener, mBarometerSensor,
          (int) (mSamplingRateInMSecs * 1000));
      setStarted(true);
    }
  }


  /**
   * this stops the barometer
   */
  public void stopBarometer() {
    if (standardPressureSensorAvailable()) {
      try {
        mSensorManager.unregisterListener(mPressureListener);
      } catch (Exception e) {
        // probably already unregistered
      }
    }
    setStarted(false);
  }

  /**
   * returns true if the barometer is currently working
   */
  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean started) {
    this.started = started;
  }
}
