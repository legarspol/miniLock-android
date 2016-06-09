package com.uliamar.minilock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.uliamar.minilock.R;

public class AboutActivity extends AppCompatActivity {
  public static Intent createIntent(FragmentActivity activity) {
    return new Intent(activity, AboutActivity.class);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    ((TextView) findViewById(R.id.about1)).setMovementMethod(LinkMovementMethod.getInstance());
    ((TextView) findViewById(R.id.about2)).setMovementMethod(LinkMovementMethod.getInstance());
  }
}
