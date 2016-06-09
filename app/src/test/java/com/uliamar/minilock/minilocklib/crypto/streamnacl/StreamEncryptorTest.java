package com.uliamar.minilock.minilocklib.crypto.streamnacl;

import com.uliamar.minilock.minilocklib.Crypto;
import com.uliamar.minilock.minilocklib.crypto.Base58;
import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class StreamEncryptorTest extends TestCase {

  final int MAX_CHUNK_LENGTH = 65535;
  List<byte[]> encryptedChunks = new ArrayList<>();
  boolean isLast;
  final byte[] key = byteSequence(32);
  final byte[] nonce = byteSequence(16);
  final byte[] data = byteSequence(1024 * 1024 + 111);

  private byte[] byteSequence(int len) {
    byte[] a = new byte[len];
    for (int i = 0; i < a.length; i++) a[i] = (byte) (i & 0xff);
    return a;
  }

  // Decrypt.
  private List<byte[]> decryptChunks(List<byte[]> chunks) throws StreamNaclBase
      .StreamNaclException {
    List<byte[]> decryptedChunks = new ArrayList<>();
    StreamDecryptor d = new StreamDecryptor(key, nonce, null);
    for (int i = 0; i < chunks.size(); i++) {
      isLast = (i == chunks.size() - 1);
      byte[] dc = d.decryptChunk(chunks.get(i), isLast);
      if (dc == null) return null;
      decryptedChunks.add(dc);
    }

    try {
      d.decryptChunk(chunks.get(0), null);
      fail("should throw if decryptChunk called after last chunk");
    } catch (StreamNaclBase.StreamNaclException ignored) {

    }

    d.clean();
    return decryptedChunks;
  }

  // Compare.
  private boolean compareChunksWithData(List<byte[]> chunks) {
    int pos = 0;
    for (int i = 0; i < chunks.size(); i++) {
      byte[] c = chunks.get(i);
      for (int j = 0; j < c.length; j++, pos++) {
        if (c[j] != data[pos]) return false;
      }
    }
    return true;
  }

  public void testEncryptChunk() throws Exception {

    // Encrypt.
    StreamEncryptor e = new StreamEncryptor(key, nonce, null);
    for (int i = 0; i < data.length; i += MAX_CHUNK_LENGTH) {
      int chunkLen = Math.min(MAX_CHUNK_LENGTH, data.length - i);
      isLast = (data.length - i - chunkLen == 0);
      byte[] ec = e.encryptChunk(Uint8Array.subarray(data, i, i + chunkLen), isLast);
      encryptedChunks.add(ec);
    }

  /*// debugging
  encryptedChunks.forEach(function(chunk) {
    console.log((new Buffer(chunk)).toString('hex'));
  });
  */
    try {
      e.encryptChunk(Uint8Array.subarray(data, 0, 1000), null);
      fail("should throw if encryptChunk was called after the last chunk");
    } catch (StreamNaclBase.StreamNaclException ignored) {

    }
    e.clean();


    List<byte[]> decryptedChunks = decryptChunks(encryptedChunks);
    assertNotNull("should decrypt chunks", decryptedChunks);
    assertEquals("number of decrypted chunks should be equal to encrypted", decryptedChunks.size
        (), encryptedChunks.size());
    assertTrue("decrypted data should be equal to original", compareChunksWithData
        (decryptedChunks));



    // Drop last chunk.
    List<byte[]> badEncryptedChunks = new ArrayList<>();
    for (int i = 0; i < encryptedChunks.size()-1; i++) badEncryptedChunks.add(encryptedChunks.get(i));
    assertNull("should not decrypt when missing last chunk", decryptChunks(badEncryptedChunks));

    // Drop first chunk.
    badEncryptedChunks.clear();
    for (int i = 1; i < encryptedChunks.size(); i++) badEncryptedChunks.add(encryptedChunks.get(i));
    assertNull("should not decrypt when missing first chunk", decryptChunks(badEncryptedChunks));

    // Drop second chunk.
    badEncryptedChunks.clear();
    for (int i = 0; i < encryptedChunks.size(); i++) if (i != 1) badEncryptedChunks.add(encryptedChunks.get(i));
    assertNull("should not decrypt when missing second chunk", decryptChunks(badEncryptedChunks));



  }


//  // Key derivation test.
//  QUnit.asyncTest('deriveKey', function(assert) {
//    'use strict';
//    var passphrase = 'This passphrase is supposed to be good enough for miniLock. :-)'
//    miniLock.crypto.getKeyPair(passphrase, 'miniLockScrypt..', function(keyPair) {
//      miniLock.session.keys = keyPair
//      miniLock.session.keyPairReady = true
//    })
//    assert.deepEqual(miniLock.session.keyPairReady, false, 'keyPairReady starts as false')
//    assert.deepEqual(Object.keys(miniLock.session.keys).length, 0, 'sessionKeys is empty')
//    var keyInterval = setInterval(function() {
//      if (miniLock.session.keyPairReady) {
//        clearInterval(keyInterval)
//        assert.deepEqual(Object.keys(miniLock.session.keys).length, 2, 'sessionKeys is filled')
//        assert.deepEqual(miniLock.session.keyPairReady, true, 'keyPairReady set to true')
//        assert.deepEqual(typeof(miniLock.session.keys), 'object', 'Type check')
//        assert.deepEqual(typeof(miniLock.session.keys.publicKey), 'object', 'Public key type check')
//        assert.deepEqual(typeof(miniLock.session.keys.secretKey), 'object', 'Secret key type check')
//        assert.deepEqual(miniLock.session.keys.publicKey.length, 32, 'Public key length')
//        assert.deepEqual(miniLock.session.keys.secretKey.length, 32, 'Secret key length')
//        assert.deepEqual(
//            Base58.encode(miniLock.session.keys.publicKey),
//            'EWVHJniXUFNBC9RmXe45c8bqgiAEDoL3Qojy2hKt4c4e',
//            'Public key Base58 representation'
//        )
//        assert.deepEqual(
//            nacl.util.encodeBase64(miniLock.session.keys.secretKey),
//            '6rcsdGAhF2rIltBRL+gwvQTQT7JMyei/d2JDrWoo0yw=',
//            'Secret key Base64 representation'
//        )
//        assert.deepEqual(
//            miniLock.crypto.getMiniLockID(miniLock.session.keys.publicKey),
//            '22d9pyWnHVGQTzCCKYEYbL4YmtGfjMVV3e5JeJUzLNum8A',
//            'miniLock ID from public key'
//        )
//        QUnit.start()
//      }
//    }, 500)
//  })



  public void testGetKeyPair() throws Exception {
    String passphrase = "This passphrase is supposed to be good enough for miniLock. :-)";
    TweetNaclFast.Box.KeyPair keyPair = Crypto.getKeyPair(passphrase, "miniLockScrypt..");
    assertEquals("public key length", keyPair.getPublicKey().length, 32);
    assertEquals("secret key length", keyPair.getSecretKey().length, 32);
    assertEquals("Public key Base58 representation", "EWVHJniXUFNBC9RmXe45c8bqgiAEDoL3Qojy2hKt4c4e", Base58.encode(keyPair.getPublicKey()));
    assertEquals("Secret key Base64 representation", "6rcsdGAhF2rIltBRL+gwvQTQT7JMyei/d2JDrWoo0yw=", Base58.encode(keyPair.getSecretKey()));
    assertEquals("miniLock ID from public key", "22d9pyWnHVGQTzCCKYEYbL4YmtGfjMVV3e5JeJUzLNum8A", Crypto.getMiniLockID(keyPair.getPublicKey()));
  }
}