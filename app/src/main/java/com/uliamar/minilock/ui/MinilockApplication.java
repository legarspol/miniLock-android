package com.uliamar.minilock.ui;

import android.app.Application;
import android.content.Context;

public class MinilockApplication extends Application {


  private SessionInformations sessionInformations;

  public static MinilockApplication get(Context context) {
    return (MinilockApplication) context.getApplicationContext();
  }

  public SessionInformations getSessionInformations() {
    return sessionInformations;
  }

  public void setSessionInformations(SessionInformations sessionInformations) {
    this.sessionInformations = sessionInformations;
  }
}
