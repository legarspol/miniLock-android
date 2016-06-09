package com.uliamar.minilock.minilocklib.crypto.streamnacl;

import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;

public class StreamEncryptor extends StreamNaclBase {


  public StreamEncryptor(byte[] key, byte[] nonce, Integer maxChunkLength) throws StreamNaclException {
    checkArgs(key, nonce, maxChunkLength);
    _key = key;
    _fullNonce = new byte[24];
    Uint8Array.set(_fullNonce, nonce);

    _maxChunkLength = maxChunkLength == null ? DEFAULT_MAX_CHUNK : maxChunkLength;
    _in = new byte[ZEROBYTES + _maxChunkLength];
    _out = new byte[ZEROBYTES + _maxChunkLength];
    _done = false;
  }

  public byte[] encryptChunk(byte[] chunk, Boolean isLast) throws StreamNaclException {
    if (this._done) throw new StreamNaclException("called encryptChunk after last chunk");
    int chunkLen = chunk.length;
    if (chunkLen > this._maxChunkLength)
      throw new StreamNaclException("chunk is too large: " + chunkLen + " / " + this._maxChunkLength);
    for (int i = 0; i < ZEROBYTES; i++) this._in[i] = 0;
    Uint8Array.set(_in, chunk, ZEROBYTES);
    if (isLast != null && isLast) {
      setLastChunkFlag(this._fullNonce);
      this._done = true;
    }
    TweetNaclFast.crypto_secretbox(this._out, this._in, chunkLen + ZEROBYTES, this._fullNonce,
        this._key);
    incrementChunkCounter(this._fullNonce);
    byte[] encryptedChunk = Uint8Array.subarray(_out, BOXZEROBYTES-4, BOXZEROBYTES-4 + chunkLen+16+4);
    encryptedChunk[0] = (byte)((chunkLen >>>  0) & 0xff);
    encryptedChunk[1] = (byte)((chunkLen >>>  8) & 0xff);
    encryptedChunk[2] = (byte)((chunkLen >>> 16) & 0xff);
    encryptedChunk[3] = (byte)((chunkLen >>> 24) & 0xff);
    return encryptedChunk;
  }


  
}
