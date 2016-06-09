package com.uliamar.minilock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.uliamar.minilock.ui.utils.MiniLockLifecycle;

public class FileDetailsActivity extends AppCompatActivity {
  public static final String KEY_FILENAME = "KEY_FILENAME";

  public static Intent createIntent(Context context, String fileName) {
    Intent intent = new Intent(context, FileDetailsActivity.class);
    intent.putExtra(KEY_FILENAME, fileName);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (MiniLockLifecycle.handleSessionInformationLoss(this)) return;

    String fileName = getIntent().getStringExtra(KEY_FILENAME);

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .add(android.R.id.content, FileDetailsFragment.newInstance(fileName))
          .commit();
    }
  }
}
