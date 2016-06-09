package com.uliamar.minilock.minilocklib;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Progress {
  private static final String TAG = Progress.class.getSimpleName();
  private static int percent;
  private static List<Listener> listeners = new ArrayList<>();

  public static void update(Float size, Float dataPostion) {
    if (size == null || dataPostion == null || size <= 0) {
      Log.e(TAG, "update() called with " + "size = [" + size + "], dataPostion = [" + dataPostion +
          "]");
      return;
    }
    if (dataPostion <= 0) {
      percent = 0;
    }
    percent = (int) (100 * dataPostion / size);

    notifyListener();
  }

  private static void notifyListener() {
    for (Listener listener : listeners) {
      listener.onProgressUpdate(percent);
    }
  }

  public static interface Listener {
    void onProgressUpdate(int percent);
  }

  public static void addListener(Listener l) {
    listeners.add(l);
  }

  public static void removeListener(Listener l) {
    listeners.remove(l);
  }

  public static void clearListeners() {
    listeners.clear();
  }
}
