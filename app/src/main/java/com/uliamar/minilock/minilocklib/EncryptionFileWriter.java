package com.uliamar.minilock.minilocklib;

import android.util.Log;

import com.uliamar.minilock.android.FileUtils;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.Uint8Array;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EncryptionFileWriter {
  private static final String MINILOCK_TMP_EXTENSION = ".miniLockTmp";
  private static final String TAG = EncryptionFileWriter.class.getSimpleName();
  private static final int CHUNCKSIZE = 1024 * 1024;
  private String fileName;
  private String tmpFileName;

  public EncryptionFileWriter(String fileName) throws MiniLockException {
    this.fileName = fileName;
    this.tmpFileName = fileName + MINILOCK_TMP_EXTENSION;
    removeTmpFileIfExisting(tmpFileName);
  }

  private static void removeTmpFileIfExisting(String tmpFileName) throws MiniLockException {
    File minilockDir = FileUtils.getFileStorageDir();
    File file = new File(minilockDir, tmpFileName);
    if (file.exists()) {
      if (!file.delete()) {
        throw new MiniLockException(MiniLockException.CANT_REMOVE_TMP_FILE);
      }
    }
  }

  private static void writeFile(byte[] encryptedChunk, String saveName) throws MiniLockException {
    File minilockDir = FileUtils.getFileStorageDir();
    File fileToOpen = new File(minilockDir, saveName);
    try {
      FileOutputStream outputStream = new FileOutputStream(fileToOpen, true);
      outputStream.write(encryptedChunk);
      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Can't write Dat file.");
      throw new MiniLockException(MiniLockException.CANT_WRITE_FILE);
    }
  }

  private static byte[] read(int start, int end, String fileName) {

    try {
      Log.d(TAG, "read() called with " + "file = [" + fileName + "], start = [" + start + "], end" +
          " = ["
          + end + "]");
      InputStream ios = null;
      int nbByteToRead = end - start;
      byte[] buffer = new byte[nbByteToRead];
      byte[] output;
      try {
        File minilockDir = FileUtils.getFileStorageDir();
        File fileToOpen = new File(minilockDir, fileName);
        ios = new FileInputStream(fileToOpen);
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

  public void add(byte[] decryptedChunk) throws MiniLockException {
    writeFile(decryptedChunk, tmpFileName);
  }

  public String getFileName() {
    return fileName;
  }

  public void prependByteAndFinishFile(List<byte[]> beginningOfFile) throws MiniLockException {
    for (byte[] bytes : beginningOfFile) {
      writeFile(bytes, fileName);
    }
    copyTmptoFile();
    File minilockDir = FileUtils.getFileStorageDir();
    File fileToOpen = new File(minilockDir, tmpFileName);
    //noinspection ResultOfMethodCallIgnored
    fileToOpen.delete();
  }

  private void copyTmptoFile() throws MiniLockException {
    int s = 0;
    int e = CHUNCKSIZE;
    byte[] bytes;
    do {
      bytes = read(s, e, tmpFileName);
      s += CHUNCKSIZE;
      e += CHUNCKSIZE;
      writeFile(bytes, fileName);
    } while (bytes.length == CHUNCKSIZE);
  }
}
