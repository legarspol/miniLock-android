package com.uliamar.minilock.minilocklib;

import com.uliamar.minilock.android.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DecryptionFileWriter {

  private String fileName;

  public DecryptionFileWriter() {
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
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

  public void add(byte[] decryptedChunk) throws MiniLockException {
    writeFile(decryptedChunk, fileName);
  }
}
