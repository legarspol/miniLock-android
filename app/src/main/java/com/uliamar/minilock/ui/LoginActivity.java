package com.uliamar.minilock.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      LoginFragment loginFragment = new LoginFragment();
      loginFragment.setArguments(getIntent().getExtras());
      getSupportFragmentManager()
          .beginTransaction()
          .add(android.R.id.content, loginFragment)
          .commit();
    }
  }
}
