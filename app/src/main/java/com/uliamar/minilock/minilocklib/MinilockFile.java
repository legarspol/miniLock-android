package com.uliamar.minilock.minilocklib;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.uliamar.minilock.android.FileUtils;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.Uint8Array;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MinilockFile implements Parcelable {
  public static final Creator<MinilockFile> CREATOR = new Creator<MinilockFile>() {
    @Override
    public MinilockFile createFromParcel(Parcel in) {
      return new MinilockFile(in);
    }

    @Override
    public MinilockFile[] newArray(int size) {
      return new MinilockFile[size];
    }
  };
  private static final String TAG = MinilockFile.class.getSimpleName();
  public String name;
  public long size;
  public Uri uri;

  public MinilockFile(String name, Long size, Uri uri) {
    this.name = name;
    this.size = size;
    this.uri = uri;
  }

  protected MinilockFile(Parcel in) {
    name = in.readString();
    size = in.readLong();
    uri = in.readParcelable(Uri.class.getClassLoader());
  }


  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeLong(size);
    dest.writeParcelable(uri, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  private InputStream getInputStream(Context context) {
    try {
//      if (file != null) {
//        return new FileInputStream(file);
//      } else
//
      if (uri != null) {
        return context.getContentResolver().openInputStream(uri);

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
  public byte[] read(Context context, int start, int end) {

    try {
      Log.d(TAG, "read() called with " + "file = [" + name + "], start = [" + start + "], end = ["
          + end + "]");
      InputStream ios = null;
      int nbByteToRead = end - start;
      byte[] buffer = new byte[nbByteToRead];
      byte[] output;
      try {
        ios = getInputStream(context);
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

  public static boolean fileExist(String saveName) {
    File minilockDir = FileUtils.getFileStorageDir();
    File fileToOpen = new File(minilockDir, saveName);
    return fileToOpen.exists();
  }

}
