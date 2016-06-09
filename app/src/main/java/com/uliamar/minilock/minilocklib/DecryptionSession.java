package com.uliamar.minilock.minilocklib;

import com.uliamar.minilock.minilocklib.crypto.streamnacl.StreamDecryptor;

public class DecryptionSession extends Session {
  private StreamDecryptor streamDecryptor;
  private String outPutFileName;

  public StreamDecryptor getStreamDecryptor() {
    return streamDecryptor;
  }

  public void setStreamDecryptor(StreamDecryptor streamDecryptor) {
    this.streamDecryptor = streamDecryptor;
  }

  public String getOutPutFileName() {
    return outPutFileName;
  }

  public void setOutPutFileName(String outPutFileName) {
    this.outPutFileName = outPutFileName;
  }


}
