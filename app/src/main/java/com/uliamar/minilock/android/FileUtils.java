package com.uliamar.minilock.android;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.uliamar.minilock.minilocklib.Minilock;

import java.io.File;

public class FileUtils {
  private static final String TAG = FileUtils.class.getSimpleName();

  public static File getFileStorageDir() {
    Log.d(TAG, "Is external storage writable " + isExternalStorageWritable());
    // Get the directory for the user's public pictures directory.
    File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOCUMENTS), Minilock.MINILOCK_DIRECTORY);
    if (!file.exists()) {
      if (!file.mkdirs()) {
        Log.e(TAG, "Directory not created");
        return null;
      }
    }
    return file;
  }

  public static String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.Images.Media.DATA};
      cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  public static FileMeta getFileMetaData(Activity activity, Uri uri) {

    // The query, since it only applies to a single document, will only return
    // one row. There's no need to filter, sort, or select fields, since we want
    // all fields for one document.
    Cursor cursor = activity.getContentResolver()
        .query(uri, null, null, null, null, null);

    try {
      // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
      // "if there's anything to look at, look at it" conditionals.
      if (cursor != null && cursor.moveToFirst()) {

        // Note it's called "Display Name".  This is
        // provider-specific, and might not necessarily be the file name.
        String displayName = cursor.getString(
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        Log.i(TAG, "Display Name: " + displayName);

        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        // If the size is unknown, the value stored is null.  But since an
        // int can't be null in Java, the behavior is implementation-specific,
        // which is just a fancy term for "unpredictable".  So as
        // a rule, check if it's null before assigning to an int.  This will
        // happen often:  The storage API allows for remote files, whose
        // size might not be locally known.
        String size = null;
        if (!cursor.isNull(sizeIndex)) {
          // Technically the column stores an int, but cursor.getString()
          // will do the conversion automatically.
          size = cursor.getString(sizeIndex);
        }
        return new FileMeta(displayName, size);
      }
    } finally {
      cursor.close();
    }
    return null;
  }

  /* Checks if external storage is available for read and write */
  public static boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      return true;
    }
    return false;
  }

  /* Checks if external storage is available to at least read */
  public boolean isExternalStorageReadable() {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state) ||
        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      return true;
    }
    return false;
  }

  public static class FileMeta {
    String name;
    String size;

    public FileMeta(String name, String size) {
      this.name = name;
      this.size = size;
    }

    public String getName() {
      return name;
    }

    public String getSize() {
      return size;
    }
  }

}
