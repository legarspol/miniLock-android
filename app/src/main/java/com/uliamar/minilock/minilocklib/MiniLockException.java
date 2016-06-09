package com.uliamar.minilock.minilocklib;

public class MiniLockException extends Exception {
  public static final int CANT_WRITE_FILE = 0;
  public static final int INVALID_HEADER_VERSION = 1;
  //Exception("miniLock: Decryption failed - invalid header version");
  public static final int COULD_NOT_PARSE_HEADER = 2;
//Exception("miniLock: Decryption failed - could not parse header");
  public static final int NOT_ENCRYPTED_FOR_THIS_RECIPIENT = 3;
  //"miniLock: Decryption failed - File is not encrypted for this recipient"
  public static final int COULDNT_VALIDATE_SENDER_ID = 4;
  //"miniLock: Decryption failed - could not validate sender ID"
  public static final int UTF_8_NOT_HANDLED = 5;

  public static final int NACL_ERROR = 6;
  public static final int GENERAL_DECRYPTION_ERROR = 7;
//  "miniLock: Decryption failed - general decryption error"

  public static final int COULDNT_VALIDATE_FILE_DECRYPTION = 8;
  //"miniLock: Decryption failed - could not validate file contents after decryption"

  public static final int FILE_ALREADY_EXISTS = 9;

  public static final int CANT_REMOVE_TMP_FILE = 10;

  public static final int DO_NOT_HAVE_READ_OR_WRITE_RIGHT= 11;

  private int reason;

  public MiniLockException(int reason) {
    this.reason = reason;
  }

  public MiniLockException(int reason, Throwable cause) {
    super(cause);
    this.reason = reason;
  }

  public int getReason() {
    return reason;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }
}
