package com.uliamar.minilock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import static com.uliamar.minilock.ui.utils.MiniLockLifecycle.handleSessionInformationLoss;

public class FilePickerActivity extends AppCompatActivity {

  public static Intent createIntent(Context context) {
    return new Intent(context, FilePickerActivity.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    if (handleSessionInformationLoss(this)) return;
    setTitle("Pick a file");

    if (savedInstanceState == null) {
      // During initial setup, plug in the details fragment.

      getSupportFragmentManager()
          .beginTransaction()
          .add(android.R.id.content, FilePickerFragment.newInstance())
          .commit();
    }
  }
}
