package com.uliamar.minilock.ui;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.uliamar.minilock.android.FileUtils;
import com.uliamar.minilock.minilocklib.Minilock;
import com.uliamar.minilock.minilocklib.MinilockFile;
import com.uliamar.minilock.minilocklib.MinilockFileReader;

import java.io.File;

import de.greenrobot.event.EventBus;

class MagicCodeReader extends AsyncTask<String, Void, MagicCodeReadedEvent> {
  private final Context context;


  public MagicCodeReader(Context context) {
    this.context = context;
  }

  @Override
  protected MagicCodeReadedEvent doInBackground(String... params) {

    String param = params[0];
    File minilockDir = FileUtils.getFileStorageDir();
    File fileToOpen = new File(minilockDir, param);

    MinilockFile file = new MinilockFile(param, fileToOpen.length(), Uri.fromFile(fileToOpen));
    try {
      return new MagicCodeReadedEvent(file, isMiniLockFile(file));
    } catch (Exception e) {
      e.printStackTrace();
      return new MagicCodeReadedEvent(null, null);
    }
  }

  @Override
  protected void onPostExecute(MagicCodeReadedEvent e) {
    super.onPostExecute(e);
    EventBus.getDefault().post(e);
  }

  private boolean isMiniLockFile(MinilockFile minilockFile) throws Exception {
    byte[] miniLockMagicCode = Minilock.FILE_MAGIC_CODE.getBytes("utf-8");
    byte[] magicCode = new MinilockFileReader(minilockFile, context).read(0, 8);
    for (int i = 0; i < miniLockMagicCode.length; ++i) {
      if (miniLockMagicCode[i] != magicCode[i])
        return false;
    }

    return true;
  }
}
