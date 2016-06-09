package com.uliamar.minilock.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.Minilock;
import com.uliamar.minilock.minilocklib.MinilockFile;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class FileDetailsFragment extends Fragment {
  private static final String TAG = FileDetailsFragment.class.getSimpleName();
  private static final String PARAM_FILENAME = "PARAM_FILENAME";
  @Bind(R.id.file_name) TextView fileNameTv;
  @Bind(R.id.cryptButton) TextView cryptButton;
  @Bind(R.id.binaryLogo) ImageView imageView;

  private String fileName;

  public static FileDetailsFragment newInstance(String fileName) {
    FileDetailsFragment f = new FileDetailsFragment();
    Bundle args = new Bundle();
    args.putString(PARAM_FILENAME, fileName);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.file_details, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_delete) {
      deleteButton();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
    fileName = getArguments().getString(PARAM_FILENAME);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
  Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_file_details, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    fileNameTv.setText(fileName);
    cryptButton.setText(fileName.matches(".*\\.minilock$") ? "Decrypt" : "Crypt");
    if (getMime(Uri.fromFile(new File(getFilePath()))).indexOf("image/") == 0) {

      Glide.with(this).load("file://" + getFilePath()).into(imageView);
    }

  }

  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @OnClick(R.id.shareButton)
  public void shareFileButton() {
    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
    String filePath = getFilePath();
    Log.d(TAG, "shareFileButton: " + filePath);
    File fileWithinMyDir = new File(filePath);

    if (fileWithinMyDir.exists()) {
      intentShareFile.setType(getMime(Uri.fromFile(fileWithinMyDir)));
      intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
      startActivity(Intent.createChooser(intentShareFile, "Share File"));
    } else {
      Log.d(TAG, "file doesn't exist here");
    }
  }

  private String getMime(Uri uri) {
    String mime = "*/*";
    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
    String url = uri.toString();
    if (mimeTypeMap.hasExtension(mimeTypeMap.getFileExtensionFromUrl(url))) {
      mime = mimeTypeMap.getMimeTypeFromExtension(mimeTypeMap.getFileExtensionFromUrl(url));
    }
    return mime;
  }

  @OnClick(R.id.cryptButton)
  void crypt_decrypt_button() {
    new MagicCodeReader(getContext()).execute(fileName);
  }

  @OnClick({R.id.binaryLogo, R.id.file_name})
  void openButonClick() {
    Intent intentShareFile = new Intent(Intent.ACTION_VIEW);
    String filePath = getFilePath();
    Log.d(TAG, "shareFileButton: " + filePath);
    File fileWithinMyDir = new File(filePath);
    Uri uri = Uri.fromFile(fileWithinMyDir);
    String mime = getMime(uri);
    intentShareFile.setDataAndType(uri, mime);
    if (fileWithinMyDir.exists()) {
      startActivity(Intent.createChooser(intentShareFile, "Open file"));
    } else {
      Log.d(TAG, "file doesn't exist here");
    }
  }

  @NonNull
  private String getFilePath() {
    return Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOCUMENTS) + "/" + Minilock.MINILOCK_DIRECTORY + "/" + fileName;
  }

  @Override
  public void onStop() {
    EventBus.getDefault().unregister(this);
    super.onStop();
  }

  void deleteButton() {
    new DeleteFileAsyncTask().execute(getFilePath());
  }

  public void onEvent(final FileDeletedEvent e) {
    if (e.aBoolean) {
      getActivity().finish();
    } else {
      Toast.makeText(getActivity(), "Unable to remove the file", Toast.LENGTH_SHORT).show();
    }
  }

  public void onEvent(final MagicCodeReadedEvent e) {
    Boolean isMinilockFile = e.getIsMinilockFile();
    MinilockFile file = e.getFile();
    if (isMinilockFile == null) {
      Snackbar.make(getView(), R.string.unable_check_file_magicCode, Snackbar.LENGTH_LONG).show();
    } else if (isMinilockFile) {
      //decrypt
      startActivity(CryptActivity.createIntent(getActivity(), false, file));
    } else {
      //crypt
      startActivity(CryptActivity.createIntent(getActivity(), true, file));
    }
  }

  private static class DeleteFileAsyncTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
      File fileToRemove = new File(params[0]);
      if (!fileToRemove.exists()) {
        Log.e(TAG, "File to remove do not exist");
        return false;
      }

      if (!fileToRemove.delete()) {
        Log.e(TAG, "doInBackground: File delete didn't worked");
        return false;
      }

      return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
      super.onPostExecute(aBoolean);
      EventBus.getDefault().post(new FileDeletedEvent(aBoolean));
    }

  }

  private static class FileDeletedEvent {

    protected final Boolean aBoolean;

    public FileDeletedEvent(Boolean aBoolean) {
      this.aBoolean = aBoolean;
    }
  }
}
