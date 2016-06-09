package com.uliamar.minilock.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.Minilock;
import com.uliamar.minilock.ui.utils.LceAnimator;

import java.io.File;
import java.io.FileFilter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFileFragment extends Fragment {

  public static final int STATE_LOADING = 0;
  public static final int STATE_CONTENT = 1;
  public static final int STATE_ERROR = 2;

  private static final String TAG = ListFileFragment.class.getSimpleName();
  protected File[] files = null;
  protected ListFileAdapter adapter;
  @Bind(R.id.contentView) View contentView;
  @Bind(R.id.errorView) View errorView;
  @Bind(R.id.loadingView) View loadingView;
  @Bind(R.id.recyclerView) RecyclerView recyclerView;
  @Bind(R.id.emptyView) View emptyView;
  int state = 0;

  public ListFileFragment() {
    // Required empty public constructor
  }

  public static ListFileFragment newInstance() {
    return new ListFileFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_list_file, container, false);
  }


  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    EventBus.getDefault().register(this);
    adapter = new ListFileAdapter(getContext());
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setAdapter(adapter);
    // TODO: 20/2/16 User a real item divider

    if (state == STATE_CONTENT) {
      showContent();
    } else if (state == STATE_ERROR) {
      showError();
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    if (state != STATE_ERROR) {
      fetchData();
    }

  }

  @OnClick(R.id.errorView)
  void errorViewClic() {
    fetchData();
  }

  private void fetchData() {
    showLoading();
    new ListFileAsyncTask().execute();
  }

  private void showLoading() {
    state = STATE_LOADING;
    LceAnimator.showLoading(loadingView, contentView, errorView);
  }


  private void showContent() {
    state = STATE_CONTENT;
    adapter.updateFileList(files);
    LceAnimator.showContent(loadingView, contentView, errorView);
    if (files.length == 0) {
      LceAnimator.animateShowEmptyView(emptyView);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void showError() {
    state = STATE_ERROR;
    LceAnimator.showErrorView(loadingView, contentView, errorView);
  }


  public void onEvent(final FilesListEvent e) {
    files = e.files;
    if (e.files == null) {
      showError();
    } else {
      showContent();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
    EventBus.getDefault().unregister(this);
  }

  private static class ListFileAsyncTask extends AsyncTask<Void, Void, File[]> {

    @Override
    protected File[] doInBackground(Void... params) {
      String s = Environment.getExternalStoragePublicDirectory(
          Environment.DIRECTORY_DOCUMENTS) + "/" + Minilock.MINILOCK_DIRECTORY;
      File miniLockDir = new File(s);
      if (!miniLockDir.exists()) {
        if (!miniLockDir.mkdirs()) {
          return null;
        }
      }

      return miniLockDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          if (pathname.getName().charAt(0) == '.')
            return false;
          if (pathname.isDirectory()) {
            return false;
          }
          return true;
        }
      });

    }

    @Override
    protected void onPostExecute(File[] files) {
      super.onPostExecute(files);
      EventBus.getDefault().post(new FilesListEvent(files));
    }
  }

  private static class FilesListEvent {

    protected final File[] files;

    public FilesListEvent(File[] strings) {
      this.files = strings;
    }
  }

}
