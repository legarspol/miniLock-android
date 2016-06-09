package com.uliamar.minilock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.uliamar.minilock.ui.utils.MiniLockLifecycle;

public class ListFileActivity extends AppCompatActivity {

  public static Intent createIntent(Context context) {
    Intent intent = new Intent(context, ListFileActivity.class);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (MiniLockLifecycle.handleSessionInformationLoss(this)) return;

    if (savedInstanceState == null) {

      getSupportFragmentManager()
          .beginTransaction()
          .add(android.R.id.content, ListFileFragment.newInstance())
          .commit();
    }
  }
}
