package com.uliamar.minilock.minilocklib.crypto.streamnacl;

public class StreamNaclBase {


  final int DEFAULT_MAX_CHUNK = 65535;
  final int ZEROBYTES = 32;
  final int BOXZEROBYTES = 16;

  protected boolean _done;
  protected byte[] _out;
  protected byte[] _in;
  protected int _maxChunkLength;
  protected byte[] _fullNonce;
  protected byte[] _key;


  protected void incrementChunkCounter(byte[] fullNonce) {
    for (int i = 16; i < 24; i++) {
      fullNonce[i]++;
      if (fullNonce[i] != 0) break;
    }
  }

  protected void setLastChunkFlag(byte[] fullNonce) {
    fullNonce[23] |= 0x80;
  }

  public void clean() {
    clean(this._fullNonce, this._in, this._out);
  }

  protected void clean(byte[]... arguments) {
    for (int i = 0; i < arguments.length; i++) {
      byte[] arg = arguments[i];
      for (int j = 0; j < arg.length; j++) arg[j] = 0;
    }
  }

  protected int readChunkLength(byte[] data, int offset) {
    if (data.length < offset + 4) return -1;
    return (data[offset]&0xFF) | (data[offset+1]&0xFF) << 8 |
        (data[offset+2]&0xFF) << 16 | (data[offset+3]&0xFF) << 24;
  }


  protected void checkArgs(byte[] key, byte[] nonce, Integer maxChunkLength) throws StreamNaclException {
    if (key.length != 32) throw new StreamNaclException("bad key length, must be 32 bytes");
    if (nonce.length != 16) throw new StreamNaclException("bad nonce length, must be 16 bytes");
//    if (maxChunkLength >= 0xffffffff) throw new StreamNaclException("max chunk length is too large");
//    if (maxChunkLength < 16) throw new StreamNaclException("max chunk length is too small");
  }

  public static class StreamNaclException extends Exception {
    public StreamNaclException(String m) {
      super(m);
    }
  }

}
