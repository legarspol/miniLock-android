package com.uliamar.minilock.ui;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.uliamar.minilock.R;
import com.uliamar.minilock.android.FileUtils;
import com.uliamar.minilock.minilocklib.MinilockFile;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilePickerFragment extends Fragment {
  private static final int CONTENT_PROVIDER_REQUEST_CODE = 1000;
  private static final String TAG = FilePickerFragment.class.getSimpleName();
  @Bind(R.id.minilockIDDisplay) TextView minilockIdDisplay;

  public FilePickerFragment() {
    // Required empty public constructor
  }

  public static FilePickerFragment newInstance() {
    return new FilePickerFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.file_picker, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_logout) {
      MinilockApplication.get(getContext()).setSessionInformations(null);
      getActivity().finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_file_picker, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    String miniLockId = MinilockApplication.get(getContext()).getSessionInformations()
        .getMiniLockId();
    minilockIdDisplay.setText(miniLockId);
  }


  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onStop() {
    EventBus.getDefault().unregister(this);
    super.onStop();
  }

  @OnClick(R.id.button_filePicker)
  void onFilePickerButtonClick() {
//    Intent intent = new Intent();
//    intent.setType("*/*");
//    intent.setAction(Intent.ACTION_GET_CONTENT);
//    startActivityForResult(intent, REQUEST_CODE);
//    Intent intent = new Intent();
//    intent.addCategory(Intent.CATEGORY_OPENABLE);
//    intent.setType("*/*");
//    intent.setAction(Intent.ACTION_GET_CONTENT);
//    startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE);

    // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
    // browser.
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

    // Filter to only show results that can be "opened", such as a
    // file (as opposed to a list of contacts or timezones)
    intent.addCategory(Intent.CATEGORY_OPENABLE);

    // Filter to show only images, using the image MIME data type.
    // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
    // To search for all documents available via installed storage providers,
    // it would be "*/*".
    intent.setType("*/*");

    startActivityForResult(intent, CONTENT_PROVIDER_REQUEST_CODE);

  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CONTENT_PROVIDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      FileUtils.FileMeta fileMeta = FileUtils.getFileMetaData(getActivity(), data.getData());
      if (fileMeta != null) {
        Log.d(TAG, fileMeta.getName() + " | " + fileMeta.getSize());
        MinilockFile minilockFile = new MinilockFile(fileMeta.getName(),
            Long.parseLong(fileMeta.getSize()),
            data.getData());

        new MagicMinilockFileCodeReader(getContext()).execute(minilockFile);
      } else {
        Log.d(TAG, "unable to get data for this effin file");
      }
    }
  }

  public void onEvent(final MagicCodeReadedEvent e) {
    Boolean isMinilockFile = e.getIsMinilockFile();
    MinilockFile file = e.getFile();
    if (isMinilockFile == null) {
      Snackbar.make(getView(), R.string.unable_check_file_magicCode, Snackbar.LENGTH_LONG).show();
    } else if (isMinilockFile) {
      startActivity(CryptActivity.createIntent(getActivity(), false, file));
    } else {
      startActivity(CryptActivity.createIntent(getActivity(), true, file));
    }
  }

  @OnClick(R.id.button_minilock_folder)
  void onBinaryLogoClick() {
    startActivity(ListFileActivity.createIntent(getActivity()));
  }

  @OnClick(R.id.minilockIdBadge)
  void onBadgeClick() {
    ClipboardManager clipboard = (ClipboardManager)
        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("miniLockId",
        MinilockApplication.get(getContext()).getSessionInformations().getMiniLockId());
    clipboard.setPrimaryClip(clip);
    Toast.makeText(getContext(), "Your miniLockID is copied", Toast.LENGTH_SHORT).show();

  }

}
