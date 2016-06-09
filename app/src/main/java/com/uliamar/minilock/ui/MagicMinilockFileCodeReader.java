package com.uliamar.minilock.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.uliamar.minilock.minilocklib.Minilock;
import com.uliamar.minilock.minilocklib.MinilockFile;
import com.uliamar.minilock.minilocklib.MinilockFileReader;

import de.greenrobot.event.EventBus;

class MagicMinilockFileCodeReader extends AsyncTask<MinilockFile, Void, MagicCodeReadedEvent> {
  private final Context context;


  public MagicMinilockFileCodeReader(Context context) {
    this.context = context;
  }

  @Override
  protected MagicCodeReadedEvent doInBackground(MinilockFile... params) {

    MinilockFile file = params[0];
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
