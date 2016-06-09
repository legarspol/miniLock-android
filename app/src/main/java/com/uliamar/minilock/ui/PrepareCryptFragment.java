package com.uliamar.minilock.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.MinilockFile;
import com.uliamar.minilock.minilocklib.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class PrepareCryptFragment extends Fragment {
  public static final String PARAM_FILE = "PARAM_FILE";
  public static final String FILENAME = "FILENAME";
  public static final String FILENAME_CHANGE_TEXT = "FILENAME_CHANGE_TEXT";
  protected String ownerMiniLockId;
  @Bind(R.id.recipient_container) RecipientInputHolder recipientList;
  @Bind(R.id.file_name) TextView fileNameTv;
  @Bind(R.id.file_size) TextView fileSizeTv;
  @Bind(R.id.save_with_random_name) TextView changeNameButton;
  MinilockFile file;

  public PrepareCryptFragment() {
    // Required empty public constructor
  }

  public static PrepareCryptFragment newInstance(MinilockFile file) {
    PrepareCryptFragment prepareCryptFragment = new PrepareCryptFragment();
    Bundle args = new Bundle();
    args.putParcelable(PARAM_FILE, file);
    prepareCryptFragment.setArguments(args);
    return prepareCryptFragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    file = getArguments().getParcelable(PARAM_FILE);
    if (file == null) {
      getActivity().finish();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_prepare_crypt, container, false);
  }


  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    fileNameTv.setText(file.name);
    fileSizeTv.setText(readableFileSize(file.size));
    ownerMiniLockId = MinilockApplication.get(getContext())
        .getSessionInformations().getMiniLockId();
    recipientList.setOwnerMinilockId(ownerMiniLockId);
    if (savedInstanceState == null) {
      recipientList.setInitialState();
    } else {
      fileNameTv.setText(savedInstanceState.getString(FILENAME));
      changeNameButton.setText(savedInstanceState.getString(FILENAME_CHANGE_TEXT));

    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(FILENAME, fileNameTv.getText().toString());
    outState.putString(FILENAME_CHANGE_TEXT, changeNameButton.getText().toString());
  }

  @OnClick(R.id.cryptButton)
  void onCryptButtonClick() {
    boolean areAllFieldValid = true;
    List<String> idsList = new ArrayList<>();
    RecipientInput recipientInput;
    for (int i = 0; i < recipientList.getChildCount(); ++i) {
      recipientInput = (RecipientInput) recipientList.getChildAt(i);
      if (recipientInput.isValidID()) {
        String miniLockID = recipientInput.getMiniLockID();
        if (miniLockID.length() != 0) {
          idsList.add(miniLockID);
        }
      } else {
        areAllFieldValid = false;
      }
    }

    if (idsList.size() == 0) {
      Toast.makeText(getActivity(), "There is no recipient", Toast.LENGTH_SHORT).show();
      return;
    }

    if (areAllFieldValid) {
      String[] destArray = new String[idsList.size()];
      for (int i = 0; i < idsList.size(); ++i) {
        destArray[i] = idsList.get(i);
      }
      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(android.R.id.content,
              CryptFragment.newCryptInstance(destArray, fileNameTv.getText().toString(), file))
          .commit();
    }
  }

  @OnClick(R.id.save_with_random_name)
  void onChangeNameButton() {
    if (fileNameTv.getText().equals(file.name)) {
      fileNameTv.setText(Util.getRandomFilename());
      changeNameButton.setText(R.string.use_original_name);
    } else {
      fileNameTv.setText(file.name);
      changeNameButton.setText(R.string.use_random_name);
    }
  }

  // Convert an integer from bytes into a readable file size.
// For example, 7493 becomes '7KB'.
  private String readableFileSize(long bytes) {
    long KB = bytes / 1024;
    long MB = KB / 1024;
    long GB = MB / 1024;

    if (KB < 1024) {
      return (long) Math.ceil(KB) + "KB";
    } else if (MB < 1024) {
      return (Math.round(MB * 10) / 10) + "MB";
    } else {
      return (Math.round(GB * 10) / 10) + "GB";
    }
  }
}
