package com.uliamar.minilock.ui.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.uliamar.minilock.ui.LoginActivity;
import com.uliamar.minilock.ui.MinilockApplication;

public class MiniLockLifecycle {
  private static final String TAG = MiniLockLifecycle.class.getSimpleName();

  public static boolean handleSessionInformationLoss(Activity activity) {
    if (MinilockApplication.get(activity).getSessionInformations() == null) {
      Log.d(TAG, "App loose it's data, finish this activity");
      activity.startActivity(new Intent(activity, LoginActivity.class));
      activity.finish();
      return true;
    }
    return false;
  }
}
