package com.uliamar.minilock.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.uliamar.minilock.BuildConfig;
import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.Crypto;
import com.uliamar.minilock.minilocklib.Phrase;
import com.uliamar.minilock.minilocklib.Util;
import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast.Box.KeyPair;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import de.greenrobot.event.EventBus;


public class LoginFragment extends Fragment {
  public static final String RECOMMANDATION_VISIVILITY = "RECOMMANDATION_VISIVILITY";
  public static final String HINT_TEXT = "HINT_TEXT";
  private static final String TAG = LoginFragment.class.getSimpleName();
  @Bind(R.id.mail_input) EditText mailInput;
  @Bind(R.id.passphrase_input) EditText passPhraseInput;
  @Bind(R.id.loginRecommandation) TextView loginRecommandation;
  @Bind(R.id.recommended_pass_layout) ViewGroup recommandationLayout;
  @Bind(R.id.recommended_pass) TextView recommandedPass;
  /**
   * Only call using {@link LoginFragment#getPhrase()}
   */
  private Phrase phrase;
  private boolean hasToCleanOnStop = false;


  public LoginFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    EventBus.getDefault().register(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);

    passPhraseInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          onClickValidButton();
        }
        return false;
      }
    });


    if (savedInstanceState != null) {
      if (savedInstanceState.getInt(RECOMMANDATION_VISIVILITY) == View.VISIBLE) {
        showRecommandation();
      }
      String string = savedInstanceState.getString(HINT_TEXT);
      if (string != null)
        loginRecommandation.setText(string);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
  }

  private Phrase getPhrase() {
    if (phrase == null) {
      phrase = new Phrase(new PhraseDictionaryParser(getContext()).getDictionary());
    }
    return phrase;
  }

  @OnLongClick(R.id.loginTitle)
  boolean onlongclikloginTitle() {
    if (BuildConfig.DEBUG) {
      mailInput.setText("legarspol@gmail.com");
      passPhraseInput.setText("We often use this sentence as test passphrase");
      return true;

    }
    return false;
  }

  @OnClick(R.id.showPassphraseButton)
  void clickRevealPassphrase() {
    int inputType = passPhraseInput.getInputType();
    if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
      passPhraseInput.setInputType(inputType - InputType.TYPE_TEXT_VARIATION_PASSWORD);
    } else {
      passPhraseInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD + InputType
          .TYPE_CLASS_TEXT);
    }
  }

  @OnClick(R.id.valid_input)
  void onClickValidButton() {
    String mail = mailInput.getText().toString();
    String pass = passPhraseInput.getText().toString();

    if (mail.length() == 0
        || !mail.matches("[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,20}")) {
      mailInput.setError(getActivity().getString(R.string.please_enter_valid_email));
      mailInput.requestFocus();
      return;
    }

    if (pass.length() == 0) {
      passPhraseInput.setError(getActivity().getString(R.string.please_enter_passphrase));
      passPhraseInput.requestFocus();
      return;
    }

    if (Crypto.checkKeyStrength(pass, mail)) {
      recommandationLayout.setVisibility(View.GONE);
      loginRecommandation.setText(R.string.unlocking);
      new GetKeyPairAsyncTask().execute(pass, mail);
    } else {
      showRecommandation();
    }
  }

  private void showRecommandation() {
    setAnotherRecommandation();
    loginRecommandation.setText(R.string.passphrase_too_weak);
    recommandationLayout.setVisibility(View.VISIBLE);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @OnClick(R.id.another_button)
  void setAnotherRecommandation() {
    recommandedPass.setText(getPhrase().get(7));
  }

  @OnClick(R.id.recommended_pass)
  void setRecommandedPass() {
    passPhraseInput.setText(recommandedPass.getText());
  }

  public void onEvent(KeyComputedEvent event) {

    try {
      String miniLockID = Crypto.getMiniLockID(event.keyPair.getPublicKey());
      SessionInformations sessionInformations = new SessionInformations(event.keyPair, miniLockID);
      MinilockApplication.get(getContext()).setSessionInformations(sessionInformations);
      Log.d(TAG, miniLockID);
      if (Util.validateID(miniLockID)) {
        getActivity().startActivity(FilePickerActivity.createIntent(getActivity()));
        Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
        hasToCleanOnStop = true;
        getActivity().finish();
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (getView() != null) {
        Snackbar.make(getView(), R.string.unable_retrieve_keypair, Snackbar.LENGTH_LONG).show();
        loginRecommandation.setText(R.string.login_contextual_hint);
        Answers.getInstance().logLogin(new LoginEvent().putSuccess(false));
      }
    }

  }

  @Override
  public void onStop() {
    super.onStop();
    cleanLoginFragment();
    hasToCleanOnStop = false;
  }

  private void cleanLoginFragment() {
    mailInput.setText(null);
    mailInput.setError(null);
    passPhraseInput.setText(null);
    passPhraseInput.setError(null);
    recommandationLayout.setVisibility(View.GONE);
    setAnotherRecommandation();
    loginRecommandation.setText(R.string.login_contextual_hint);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (hasToCleanOnStop == false) {
      outState.putInt(RECOMMANDATION_VISIVILITY, recommandationLayout.getVisibility());
      outState.putString(HINT_TEXT, loginRecommandation.getText().toString());
    }
  }


  @OnClick(R.id.aboutbutton)
  public void onAboutClick() {
    startActivity(AboutActivity.createIntent(getActivity()));
  }


  public static class GetKeyPairAsyncTask extends AsyncTask<String, Void, KeyPair> {

    @Override
    protected KeyPair doInBackground(String... params) {
      try {
        return Crypto.getKeyPair(params[0], params[1]);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        Log.e(TAG, "Couldn't get KeyPair", e);
        return null;
      }
    }

    @Override
    protected void onPostExecute(KeyPair keyPair) {
      super.onPostExecute(keyPair);
      EventBus.getDefault().post(new KeyComputedEvent(keyPair));
    }
  }

  private static class KeyComputedEvent {
    KeyPair keyPair;

    public KeyComputedEvent(KeyPair keyPair) {
      this.keyPair = keyPair;
    }
  }
}
