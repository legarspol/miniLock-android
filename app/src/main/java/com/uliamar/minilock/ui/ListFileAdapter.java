package com.uliamar.minilock.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uliamar.minilock.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListFileAdapter extends RecyclerView.Adapter {
  private Context context;
  private List<File> files = new ArrayList<>();

  public ListFileAdapter(Context context) {
    this.context = context;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view = LayoutInflater.from(context).inflate(R.layout.row_file_list, parent, false);
    return new FileViewHolder(view);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    final String name = files.get(position).getName();
    ((FileViewHolder) holder).textView.setText(name);
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        context.startActivity(FileDetailsActivity.createIntent(context, name));
      }
    });
  }

  @Override
  public int getItemCount() {
    return files.size();
  }

  public void updateFileList(File[] files) {
    this.files.clear();
    this.files.addAll(Arrays.asList(files));
    notifyDataSetChanged();
  }

   static class FileViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.textview) TextView textView;

    public FileViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

}
