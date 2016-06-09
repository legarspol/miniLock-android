package com.uliamar.minilock.minilocklib;

import android.content.Context;
import android.util.Log;

import com.uliamar.minilock.minilocklib.crypto.streamnacl.Uint8Array;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MinilockFileReader {
  private static final String TAG = MinilockFileReader.class.getSimpleName();

  private final MinilockFile fileMeta;
  private final Context context;

  public MinilockFileReader(MinilockFile fileMeta, Context context) {
    this.fileMeta = fileMeta;
    this.context = context;
  }

  private InputStream getInputStream() {
    try {
//      if (file != null) {
//        return new FileInputStream(file);
//      } else
      if (fileMeta.uri != null) {
        return context.getContentResolver().openInputStream(fileMeta.uri);

      } else {
        throw new RuntimeException("Couldn't get input stream to read");
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("File not found");
    }
  }

  // Input: File object, bounds within which to read, and callbacks
// Output: Callback function executed with object:
//	{
//		name: File name,
//		size: File size (bytes),
//		data: File data within specified bounds (Uint8Array)
//	}
// Error callback which is called in case of error (no parameters)
  public byte[] read(int start, int end) {

    try {
      Log.d(TAG, "read() called with " + "file = [" + fileMeta.name + "], start = [" + start +
          "], end = ["
          + end + "]");
      InputStream ios = null;
      int nbByteToRead = end - start;
      byte[] buffer = new byte[nbByteToRead];
      byte[] output;
      try {
        ios = getInputStream();
        ios.skip(start);
        int readed = ios.read(buffer, 0, nbByteToRead);
        output = Uint8Array.subarray(buffer, 0, readed);
      } finally {
        try {
          if (ios != null) {
            ios.close();
          }
        } catch (IOException ignored) {

        }
      }
      return output;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Couldn't open the file");
    }
  }

}
