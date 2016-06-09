package com.uliamar.minilock.minilocklib;

import android.os.Parcel;
import android.os.Parcelable;

public class EncryptParams implements Parcelable {
  public static final Creator<EncryptParams> CREATOR = new Creator<EncryptParams>() {
    @Override
    public EncryptParams createFromParcel(Parcel in) {
      return new EncryptParams(in);
    }

    @Override
    public EncryptParams[] newArray(int size) {
      return new EncryptParams[size];
    }
  };
  private boolean isCrypt; //false for decrypt
  private MinilockFile minilockFile;
  private String outputFileName;
  private String[] recipients;
  private String miniLockId;
  private byte[] secretKey;

  public EncryptParams(boolean isCrypt, MinilockFile minilockFile, String encryptedFileName,
                       String[] recipients, String miniLockId, byte[] secretKey) {
    this.isCrypt = isCrypt;
    this.minilockFile = minilockFile;
    this.outputFileName = encryptedFileName;
    this.recipients = recipients;
    this.miniLockId = miniLockId;
    this.secretKey = secretKey;
  }

  protected EncryptParams(Parcel in) {
    isCrypt = in.readByte() != 0;
    minilockFile = in.readParcelable(MinilockFile.class.getClassLoader());
    outputFileName = in.readString();
    recipients = in.createStringArray();
    miniLockId = in.readString();
    secretKey = in.createByteArray();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte((byte) (isCrypt ? 1 : 0));
    dest.writeParcelable(minilockFile, flags);
    dest.writeString(outputFileName);
    dest.writeStringArray(recipients);
    dest.writeString(miniLockId);
    dest.writeByteArray(secretKey);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public boolean isCrypt() {
    return isCrypt;
  }

  public MinilockFile getMinilockFile() {
    return minilockFile;
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public String[] getRecipients() {
    return recipients;
  }

  public String getMiniLockId() {
    return miniLockId;
  }

  public byte[] getSecretKey() {
    return secretKey;
  }
}
