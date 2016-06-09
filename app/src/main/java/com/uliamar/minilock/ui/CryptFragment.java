package com.uliamar.minilock.ui;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.EncryptParams;
import com.uliamar.minilock.minilocklib.MiniLockException;
import com.uliamar.minilock.minilocklib.MiniLockService;
import com.uliamar.minilock.minilocklib.Minilock;
import com.uliamar.minilock.minilocklib.MinilockFile;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.uliamar.minilock.minilocklib.MiniLockService.BROADCAST_ACTION;
import static com.uliamar.minilock.minilocklib.MiniLockService.INTENT_TYPE_PERCENT;
import static com.uliamar.minilock.minilocklib.MiniLockService.PERCENT_VALUE;
import static com.uliamar.minilock.minilocklib.MiniLockService.RESULT_EXCEPTION;
import static com.uliamar.minilock.minilocklib.MiniLockService.RESULT_FILE_NAME;
import static com.uliamar.minilock.minilocklib.MiniLockService.RESULT_IS_SUCCESS;


/**
 * A simple {@link Fragment} subclass.
 */
public class CryptFragment extends Fragment {
  private static final String TAG = CryptFragment.class.getSimpleName();
  public static final String PARAM_DESTINATAIRES = "PARAM_DESTINATAIRES";
  public static final String PARAM_ENCRYPTED_FILE_NAME = "PARAM_ENCRYPTED_FILE_NAME";
  public static final String PARAM_FILE = "PARAM_FILE";
  protected EncryptServiceReceiver receiver;
  String[] destinataire;
  @Bind(R.id.file_name) TextView fileNameTv;
  @Bind(R.id.loading) TextView loadingText;
  @Bind(R.id.errorView) View errorView;
  @Bind(R.id.errorText) TextView errorText;
  @Bind(R.id.progressBar) ProgressBar progressBar;
  public static final int REQUEST_WRITE_STORAGE = 8000;

  MiniLockException exception;
  MinilockFile file;
  private String encryptedFileName;
  private boolean isEncrypt;

  public CryptFragment() {
    // Required empty public constructor
  }

  //decrypting
  public static CryptFragment newDecryptInstance(MinilockFile file) {
    Log.d(TAG, "newDecryptInstance() called with " + "file = [" + file + "]");
    Bundle bundle = new Bundle();
    CryptFragment cryptFragment = new CryptFragment();
    bundle.putParcelable(PARAM_FILE, file);
    cryptFragment.setArguments(bundle);
    return cryptFragment;
  }

  //Crypting
  public static CryptFragment newCryptInstance(String[] destinaire, String encryptedFileName,
                                               MinilockFile file) {
    Log.d(TAG, "newCryptInstance() called with " + "destinaire = [" + destinaire + "], " +
        "encryptedFileName = [" + encryptedFileName + "], file = [" + file + "]");
    Bundle bundle = new Bundle();
    bundle.putStringArray(PARAM_DESTINATAIRES, destinaire);
    bundle.putString(PARAM_ENCRYPTED_FILE_NAME, encryptedFileName);
    bundle.putParcelable(PARAM_FILE, file);
    CryptFragment cryptFragment = new CryptFragment();
    cryptFragment.setArguments(bundle);
    return cryptFragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate() called with " + "savedInstanceState = [" + savedInstanceState + "]");
    setRetainInstance(true);
    file = getArguments().getParcelable(PARAM_FILE);
    this.destinataire = getArguments().getStringArray(PARAM_DESTINATAIRES);
    this.encryptedFileName = getArguments().getString(PARAM_ENCRYPTED_FILE_NAME);
    isEncrypt = destinataire != null;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_crypt, container, false);
  }


  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    loadingText.setText(isEncrypt ? R.string.encrypting_dotx3 : R.string.decrypting_dotx3);
    fileNameTv.setText(file.name);


    if (savedInstanceState == null) {
      int permission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission
          .WRITE_EXTERNAL_STORAGE
      );
      if (permission != PackageManager.PERMISSION_GRANTED) {
        Log.i(TAG, "onViewCreated: asking storage right");
        requestPermissions(new String[]{Manifest.permission
            .WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
      } else {
        launchCrypt();
      }
    } else {
      if (exception != null) {
        displayError(exception);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
  int[] grantResults) {
    if (requestCode == REQUEST_WRITE_STORAGE) {
      if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        Log.i(TAG, "onRequestPermissionsResult: permission denied by user");
        displayError(new MiniLockException(MiniLockException.DO_NOT_HAVE_READ_OR_WRITE_RIGHT));
      } else {
        Log.i(TAG, "onRequestPermissionsResult: permission allowed");
        launchCrypt();
      }
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void launchCrypt() {
    MinilockFile currentFile = file;
    MinilockFile miniLockFileMeta = new MinilockFile(currentFile.name,
        currentFile.size,
        currentFile.uri);
    SessionInformations sessionInformations = MinilockApplication.get
        (getContext()).getSessionInformations();
    EncryptParams serviceParams = new EncryptParams(destinataire == null ? false : true,
        miniLockFileMeta,
        encryptedFileName,
        destinataire,
        sessionInformations.getMiniLockId(),
        sessionInformations.getKeys().getSecretKey());

    Intent intent = new Intent(getActivity(), MiniLockService.class);
    intent.putExtra(MiniLockService.SERVICE_PARAMS, serviceParams);
    getActivity().startService(intent);
  }

  @Override
  public void onStart() {
    super.onStart();
    IntentFilter mStatusIntentFilter = new IntentFilter(BROADCAST_ACTION);

    receiver = new EncryptServiceReceiver();
    LocalBroadcastManager.getInstance(getActivity())
        .registerReceiver(receiver, mStatusIntentFilter);
  }

  @Override
  public void onStop() {
    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    receiver = null;
    super.onStop();
  }

  private void displayError(MiniLockException e) {
    exception = e;
    Log.d(TAG, "displayError() called with " + "e = [" + e + "]");
    errorView.setVisibility(View.VISIBLE);
    String[] stringArray = getResources().getStringArray(R.array.minilock_exception);
    errorText.setText(stringArray[e.getReason()]);
  }

  private void displaySuccess(String fileName) {
    fileName = fileName == null ? encryptedFileName + Minilock.FILE_EXTENSION : fileName;
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(android.R.id.content, FileDetailsFragment.newInstance(fileName))
        .commit();
  }

  private void displayPercent(int percent) {
    progressBar.setProgress(percent);
  }

  private class EncryptServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getStringExtra(MiniLockService.INTENT_TYPE).equals(INTENT_TYPE_PERCENT)) {
        int percent = intent.getIntExtra(PERCENT_VALUE, 0);
        displayPercent(percent);
      } else {
        boolean booleanExtra = intent.getBooleanExtra(RESULT_IS_SUCCESS, false);
        if (booleanExtra) {
          String fileName = intent.getStringExtra(RESULT_FILE_NAME);
          displaySuccess(fileName);

        } else {
          exception = (MiniLockException) intent
              .getSerializableExtra(RESULT_EXCEPTION);
          displayError(exception);
        }

      }
    }
  }

}
