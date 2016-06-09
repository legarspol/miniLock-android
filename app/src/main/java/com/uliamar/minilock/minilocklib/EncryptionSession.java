package com.uliamar.minilock.minilocklib;

import com.uliamar.minilock.minilocklib.crypto.streamnacl.StreamEncryptor;

public class EncryptionSession extends Session {
  private StreamEncryptor streamEncryptor;
  private EncryptionFileWriter encryptionFileWriter;

  public EncryptionSession(EncryptionFileWriter encryptionFileWriter) {
    this.encryptionFileWriter = encryptionFileWriter;
  }

  public StreamEncryptor getStreamEncryptor() {
    return streamEncryptor;
  }

  public void setStreamEncryptor(StreamEncryptor streamEncryptor) {
    this.streamEncryptor = streamEncryptor;
  }

  public EncryptionFileWriter getEncryptionFileWriter() {
    return encryptionFileWriter;
  }


}
