package com.android.photour;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Show a splash screen before the main activity has been loaded
 *
 * @author Zer Jun Eng & Jia Hua Ng
 */
public class SplashScreenActivity extends AppCompatActivity {

  /**
   *
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   * down then this Bundle contains the data it most recently supplied in
   */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);
    finish();
  }
}
