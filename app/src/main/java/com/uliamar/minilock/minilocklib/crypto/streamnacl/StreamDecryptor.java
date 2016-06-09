package com.uliamar.minilock.minilocklib.crypto.streamnacl;

import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;

public class StreamDecryptor extends StreamNaclBase {

  private boolean _failed;

  public StreamDecryptor(byte[] key, byte[] nonce, Integer maxChunkLength) throws StreamNaclException {
    checkArgs(key, nonce, maxChunkLength);
    _key = key;
    _fullNonce = new byte[24];
    Uint8Array.set(_fullNonce, nonce);
    _maxChunkLength = maxChunkLength == null ? DEFAULT_MAX_CHUNK : maxChunkLength;

    _in = new byte[ZEROBYTES + _maxChunkLength];
    _out = new byte[ZEROBYTES + _maxChunkLength];
    _failed = false;
    _done = false;
  }

  private byte[] fail() {
    this._failed = true;
    this.clean();
    return null;
  }

  public byte[] decryptChunk(byte[] encryptedChunk, Boolean isLast) throws StreamNaclException {
    if (this._failed) return null;
    if (this._done) throw new StreamNaclException("called decryptChunk after last chunk");
    int encryptedChunkLen = encryptedChunk.length;
    if (encryptedChunkLen < 4 + BOXZEROBYTES) return fail();
    int chunkLen = readChunkLength(encryptedChunk, 0);
    if (chunkLen < 0 || chunkLen > this._maxChunkLength) return fail();
    if (chunkLen + 4 + BOXZEROBYTES != encryptedChunkLen) return fail();
    for (int i = 0; i < BOXZEROBYTES; i++) this._in[i] = 0;
    for (int i = 0; i < encryptedChunkLen-4; i++) this._in[BOXZEROBYTES+i] = encryptedChunk[i+4];
    if (isLast != null && isLast) {
      setLastChunkFlag(this._fullNonce);
      this._done = true;
    }
    if (TweetNaclFast.crypto_secretbox_open(this._out, this._in, encryptedChunkLen+BOXZEROBYTES-4,
        this._fullNonce, this._key) != 0) return fail();
    incrementChunkCounter(this._fullNonce);
    return Uint8Array.subarray(_out, ZEROBYTES, ZEROBYTES + chunkLen);
  }

}
