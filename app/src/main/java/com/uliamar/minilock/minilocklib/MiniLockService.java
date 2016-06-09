package com.uliamar.minilock.minilocklib;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.uliamar.minilock.R;
import com.uliamar.minilock.minilocklib.Progress.Listener;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.StreamNaclBase;
import com.uliamar.minilock.ui.LoginActivity;

import java.io.UnsupportedEncodingException;

public class MiniLockService extends IntentService {

  public static final String SERVICE_PARAMS = "SERVICE_PARAMS";
  public static final String BROADCAST_ACTION = MiniLockService.class.getCanonicalName();
  public static final String INTENT_TYPE = "INTENT_TYPE";
  public static final String INTENT_TYPE_PERCENT = "INTENT_TYPE_PERCENT";
  public static final String PERCENT_VALUE = "PERCENT_VALUE";
  public static final String INTENT_TYPE_RESULT = "INTENT_TYPE_RESULT";
  public static final String RESULT_IS_SUCCESS = "RESULT_IS_SUCCESS";
  public static final String RESULT_FILE_NAME = "RESULT_FILE_NAME";
  public static final String RESULT_EXCEPTION = "RESULT_EXCEPTION";

  private static final String TAG = MiniLockService.class.getSimpleName();
  private static final int ONGOING_NOTIFICATION_ID = 1000;
  protected NotificationCompat.Builder builder;

  public MiniLockService() {
    // Needed for debug
    super("MiniLockService");
  }

  @Override
  protected void onHandleIntent(final Intent intent) {
    Listener l = new Listener() {
      @Override
      public void onProgressUpdate(int percent) {
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(INTENT_TYPE, INTENT_TYPE_PERCENT);
        intent.putExtra(PERCENT_VALUE, percent);
        LocalBroadcastManager.getInstance(MiniLockService.this).sendBroadcast(intent);
        Log.d(TAG, "percent: " + percent);
      }
    };
    Progress.addListener(l);
    EncryptParams sp = intent.getParcelableExtra(SERVICE_PARAMS);
    MinilockFile minilockFile = sp.getMinilockFile();
    ;
    Crypto.CallBackOnComplete callBackOnComplete = null;
    Intent localIntent = new Intent(BROADCAST_ACTION);
    localIntent.putExtra(INTENT_TYPE, INTENT_TYPE_RESULT);
    Log.d(TAG, "service begining");
    boolean isSuccess = false;
    try {
      Intent notificationIntent = new Intent(this, LoginActivity.class);
      builder = new NotificationCompat.Builder(this)
          .setSmallIcon(R.drawable.ic_lock_notification)
          .setContentTitle("miniLock")
          .setContentText("miniLock is Running")
          .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0));
      startForeground(ONGOING_NOTIFICATION_ID, builder.build());
      final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context
          .NOTIFICATION_SERVICE);
      Listener notifListener = new Listener() {
        @Override
        public void onProgressUpdate(int percent) {
          builder.setProgress(200, percent, false);
          mNotifyManager.notify(ONGOING_NOTIFICATION_ID, builder.build());
        }
      };
      Progress.addListener(notifListener);
      try {
        if (sp.isCrypt()) {
          String outputFileName = sp.getOutputFileName() + Minilock.FILE_EXTENSION;
          if (MinilockFile.fileExist(outputFileName)) {
            throw new MiniLockException(MiniLockException.FILE_ALREADY_EXISTS);
          }
          callBackOnComplete = Crypto.encryptFile(
              minilockFile,
              outputFileName,
              sp.getRecipients(),
              sp.getMiniLockId(),
              sp.getSecretKey(),
              new MinilockFileReader(minilockFile, this));
          isSuccess = true;

        } else {
          callBackOnComplete = Crypto.decryptFile(
              minilockFile,
              sp.getMiniLockId(),
              sp.getSecretKey(),
              new MinilockFileReader(minilockFile, this));
          isSuccess = true;
        }
        Log.d(TAG, "Done");
      } catch (StreamNaclBase.StreamNaclException e) {

        e.printStackTrace();
        throw new MiniLockException(MiniLockException.NACL_ERROR, e);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        throw new MiniLockException(MiniLockException.UTF_8_NOT_HANDLED);
      }
    } catch (MiniLockException e) {
      localIntent.putExtra(RESULT_EXCEPTION, e);
      e.printStackTrace();
    } finally {
      stopForeground(true);
      Progress.clearListeners();
    }
    Log.d(TAG, "service done");
    localIntent.putExtra(RESULT_IS_SUCCESS, isSuccess);
    if (callBackOnComplete != null && callBackOnComplete.saveName != null) {
      localIntent.putExtra(RESULT_FILE_NAME, callBackOnComplete.saveName);
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

  }

}
