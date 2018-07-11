package com.uliamar.minilock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.uliamar.minilock.minilocklib.MinilockFile;
import com.uliamar.minilock.ui.utils.MiniLockLifecycle;

public class CryptActivity extends AppCompatActivity {
  public static final String PARAM_IS_CRYPT_ACTION = "PARAM_IS_CRYPT_ACTION";
  public static final String PARAM_MINILOCKFILE = "PARAM_MINILOCKFILE";

  public static Intent createIntent(Context context, boolean isCrypt, MinilockFile file) {
    Intent intent = new Intent(context, CryptActivity.class);
    intent.putExtra(PARAM_IS_CRYPT_ACTION, isCrypt);
    intent.putExtra(PARAM_MINILOCKFILE, file);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (MiniLockLifecycle.handleSessionInformationLoss(this)) return;

    if (savedInstanceState == null) {
      readIntentAndStartFragment();
    }
  }

  private void readIntentAndStartFragment() {
    Fragment fragment;

    MinilockFile file = getIntent().getExtras().getParcelable(PARAM_MINILOCKFILE);
    if (getIntent().getBooleanExtra(PARAM_IS_CRYPT_ACTION, true)) {
      fragment = PrepareCryptFragment.newInstance(file);
      Answers.getInstance().logCustom(new CustomEvent("Encrypt Start"));
    } else {
      fragment = CryptFragment.newDecryptInstance(file);
      Answers.getInstance().logCustom(new CustomEvent("Decrypt Start"));
    }

    getSupportFragmentManager()
        .beginTransaction()
        .add(android.R.id.content, fragment)
        .commit();
  }


  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    readIntentAndStartFragment();
  }
}
