package com.uliamar.minilock.minilocklib;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.lambdaworks.crypto.SCrypt;
import com.uliamar.minilock.minilocklib.crypto.Base58;
import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;
import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast.Box;
import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast.Box.KeyPair;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.StreamDecryptor;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.StreamEncryptor;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.StreamNaclBase.StreamNaclException;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.Uint8Array;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.uliamar.minilock.minilocklib.MiniLockException.COULDNT_VALIDATE_FILE_DECRYPTION;
import static com.uliamar.minilock.minilocklib.MiniLockException.COULDNT_VALIDATE_SENDER_ID;
import static com.uliamar.minilock.minilocklib.MiniLockException.COULD_NOT_PARSE_HEADER;
import static com.uliamar.minilock.minilocklib.MiniLockException.GENERAL_DECRYPTION_ERROR;
import static com.uliamar.minilock.minilocklib.MiniLockException.INVALID_HEADER_VERSION;
import static com.uliamar.minilock.minilocklib.MiniLockException.NOT_ENCRYPTED_FOR_THIS_RECIPIENT;

public class Crypto {
  private static final String TAG = Crypto.class.getSimpleName();


  // Chunk size (in bytes)
  // Warning: Must not be less than 256 bytes
  //todo originally 1024 * 1024
  // TODO: 16/1/16 then 1 *1024
  public static int CHUNKSIZE = 1024 * 1024;


//// Generic callback to call when full file encryption is finished.
//// Input: Blob object, filename for saving, sender ID
//  miniLock.crypto.encryptionCompleteCallback = function(blob, saveName, senderID) {
//    miniLock.UI.fileOperationIsComplete({
//        name: saveName,
//        size: blob.size,
//        data: blob,
//        type: "application/minilock"
//    }, "encrypt", senderID)
//  }
//
//
//
//// Generic callback to call when full file decryption is finished.
//// Input: Blob object, filename for saving, sender ID
//  miniLock.crypto.decryptionCompleteCallback = function(blob, saveName, senderID) {
//    miniLock.UI.fileOperationIsComplete({
//        name: saveName,
//        size: blob.size,
//        data: blob,
//        type: blob.type
//    }, "decrypt", senderID)
//  }

  // Input: User key hash (Uint8Array), Salt (Uint8Array), callback function
  // Result: Calls scrypt which returns
  //	32 bytes of key material in a Uint8Array,
  //	which then passed to the callback.
  //  Throws:
  //  GeneralSecurityException - when HMAC_SHA256 is not available.
  public static byte[] getScryptKey(byte[] key, byte[] salt) throws
      GeneralSecurityException {
    byte[] keyBytes = SCrypt.scrypt(key, salt, (int) Math.pow(2, 17), 8, 1, 32);

    return keyBytes;
  }

  // Input: User key, user email
  // Output: Whether key is strong enough
  public static boolean checkKeyStrength(String key, String email) {
    if (key.length() < 32) {
      return false;
    }
    if (key.equals(email)) {
      return false;
    }
    // TODO: 29/8/15 Find a zxcvbn java implementation
    //    return (zxcvbn(key).entropy > MinilockSettings.MIN_KEY_ENTROPY);
    return true;
  }


  // Input: User key (String), User salt (email) (String), callback (function)
// Result: Passes the following object to the callback:
// {
//	publicKey: Public encryption key (Uint8Array),
//	secretKey: Secret encryption key (Uint8Array)
// }
  public static KeyPair getKeyPair(String key, String salt) throws UnsupportedEncodingException {
    byte[] keyBytes = key.getBytes("utf-8");
    byte[] saltBytes = salt.getBytes("utf-8");

    Blake2s keyHash = new Blake2s(32, null);
    keyHash.update(keyBytes);
    try {
      byte[] keyByte = getScryptKey(keyHash.digest(), saltBytes);
      KeyPair keyPair = Box.keyPair_fromSecretKey(keyByte);
      return keyPair;
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
      throw new RuntimeException();
    }

  }

  // Input: none
  // Output: nonce for usage in encryption operations
  public static byte[] getNonce() {
    return TweetNaclFast.randombytes(24);
  }

  // Input: none
  // Output: File key for usage in nacl.secretbox() encryption operations
  public static byte[] getFileKey() {
    return TweetNaclFast.randombytes(32);
  }

  // Input: Public encryption key (Uint8Array)
  // Output: miniLock ID (Base58)
  public static String getMiniLockID(byte[] publicKey) throws Exception {
    if (publicKey.length != 32) {
      throw new Exception("miniLock.crypto.getMiniLockID: invalid public key size");
    }
    byte[] id = new byte[33];
    for (int i = 0; i < publicKey.length; i++) {
      id[i] = publicKey[i];
    }

    Blake2s hash = new Blake2s(1, null);
    hash.update(publicKey);
    id[32] = hash.digest()[0];
    return Base58.encode(id);
  }

  // Input: Object:
//	{
//		name: File name,
//		size: File size,
//		data: File (ArrayBuffer),
//	}
// saveName: Name to use when saving resulting file. ".minilock" extension will be added.
// miniLockIDs: Array of (Base58) public IDs to encrypt for
// senderID: Sender"s miniLock ID (String)
// mySecretKey: My secret key (Uint8Array)
// callback: Name of the callback function to which encrypted result is passed.
// Result: Sends file to be encrypted, with the result picked up
//	 and sent to the specified callback.
  public static CallBackOnComplete encryptFile(MinilockFile file,
                                               String saveName,
                                               String[] miniLockIDs,
                                               String myMiniLockID,
                                               byte[] mySecretKey,
                                               MinilockFileReader fileReader)
      throws StreamNaclException, UnsupportedEncodingException, MiniLockException {
    EncryptionSession session = new EncryptionSession(new EncryptionFileWriter(saveName));
    session.setSourceFileName(file.name);
    byte[] fileKey = Crypto.getFileKey();

    byte[] fileNonce = Uint8Array.subarray(Crypto.getNonce(), 0, 16);

    session.setStreamEncryptor(new StreamEncryptor(
        fileKey,
        fileNonce,
        Crypto.CHUNKSIZE
    ));
    byte[] paddedFileName = new byte[256];
    byte[] fileNameBytes = file.name.getBytes("utf-8");
    if (fileNameBytes.length > paddedFileName.length) {
      throw new Error("miniLock: Encryption failed - file name is too long");
    }
    Uint8Array.set(paddedFileName, fileNameBytes);
    byte[] encryptedChunk;
    encryptedChunk = session.getStreamEncryptor().encryptChunk(
        paddedFileName,
        false
    );
    if (encryptedChunk == null) {
      throw new Error("miniLock: Encryption failed - general encryption error");
    }
    session.getHashObject().update(encryptedChunk);
    session.getEncryptionFileWriter().add(encryptedChunk);
    CallBackOnComplete callBackOnComplete = null;
    session.setDataPostion(0);
    while (callBackOnComplete == null) {
      callBackOnComplete = encryptNextChunk(
          file,
          fileKey,
          fileNonce,
          miniLockIDs,
          myMiniLockID,
          mySecretKey,
          session,
          fileReader
      );
    }
    return callBackOnComplete;
  }

  //	Input:
//		Entire file object,
//		data position on which to start decryption (number),
//		Name to use when saving the file (String),
//		fileKey (Uint8Array),
//		fileNonce (Uint8Array),
//		miniLock IDs for which to encrypt (Array),
//		sender ID (Base58 string),
//		sender long-term secret key (Uint8Array)
//		Callback to execute when last chunk has been decrypted.
//	Result: Will recursively encrypt until the last chunk,
//		at which point callbackOnComplete() is called.
//		Callback is passed these parameters:
//			file: Decrypted file object (blob),
//			saveName: File name for saving the file (String),
//			senderID: Sender's miniLock ID (Base58 string)
  private static CallBackOnComplete encryptNextChunk(MinilockFile file,
                                                     byte[] fileKey,
                                                     byte[] fileNonce,
                                                     String[] miniLockIDs,
                                                     String myMiniLockID,
                                                     byte[] mySecretKey,
                                                     EncryptionSession session,
                                                     MinilockFileReader fileReader)
      throws StreamNaclException, UnsupportedEncodingException, MiniLockException {
    Gson g = new GsonBuilder().disableHtmlEscaping().create();
    int dataPosition = session.getDataPostion();
    byte[] chunk = fileReader.read(dataPosition, dataPosition + Crypto.CHUNKSIZE);
    boolean isLast = false;
    if (dataPosition >= (file.size - Crypto.CHUNKSIZE)) {
      isLast = true;
    }
    byte[] encryptedChunk;
    encryptedChunk = session.getStreamEncryptor().encryptChunk(
        chunk,
        isLast
    );
    if (encryptedChunk == null) {
      throw new Error("miniLock: Encryption failed - general encryption error");
    }
    session.getHashObject().update(encryptedChunk);
    EncryptionFileWriter encryptionFileWriter = session.getEncryptionFileWriter();
    encryptionFileWriter.add(encryptedChunk);
//      Minilock.UI.animateProgressBar(dataPosition + miniLock.crypto.chunkSize, file.size)
    if (isLast) {
      session.getStreamEncryptor().clean();
      // Finish generating header so we can pass finished file to callback
      KeyPair ephemeral = Box.keyPair();

      Header header = new Header(1, TweetNaclFast.base64EncodeToString(ephemeral.getPublicKey()),
          new HashMap<String, String>());

      List<byte[]> decryptInfoNonces = new ArrayList<>();
      for (String miniLockID : miniLockIDs) {
        decryptInfoNonces.add(
            Crypto.getNonce()
        );
      }

      for (int i = 0; i < miniLockIDs.length; i++) {
        FileInfo fileInfo = new FileInfo(TweetNaclFast.base64EncodeToString(fileKey),
            TweetNaclFast.base64EncodeToString(fileNonce),
            TweetNaclFast.base64EncodeToString(session.getHashObject().digest()));

        DecryptInfo decryptInfo = new DecryptInfo(myMiniLockID, miniLockIDs[i]);

        byte[] decode = Base58.decode(miniLockIDs[i]);
        byte[] subarray = Uint8Array.subarray(decode, 0, 32);
        Box b = new Box(subarray, mySecretKey);
        String s = g.toJson(fileInfo);
        byte[] bytes = s.getBytes("utf-8");
        byte[] box = b.box(bytes, decryptInfoNonces.get(i));
        decryptInfo.fileInfo = TweetNaclFast.base64EncodeToString(box);


        b = new Box(Uint8Array.subarray(Base58.decode(miniLockIDs[i]), 0, 32), ephemeral
            .getSecretKey());
        String decryptInfoString = TweetNaclFast.base64EncodeToString(b.box(g.toJson(decryptInfo)
            .getBytes("utf-8"), decryptInfoNonces.get(i)));

        header.decryptInfo.put(TweetNaclFast.base64EncodeToString(decryptInfoNonces.get(i)),
            decryptInfoString);

      }
      String headerJson = g.toJson(header);
      byte[] headerLength = Util.numberToByteArray(headerJson.length());
      List<byte[]> fileEncryptedChuncks = new ArrayList<>();
      fileEncryptedChuncks.add(0, headerJson.getBytes("utf-8"));
      fileEncryptedChuncks.add(0, headerLength);
      fileEncryptedChuncks.add(0, Minilock.FILE_MAGIC_CODE.getBytes("utf-8"));
      encryptionFileWriter.prependByteAndFinishFile(fileEncryptedChuncks);
      CallBackOnComplete out = new CallBackOnComplete();
      out.saveName = encryptionFileWriter.getFileName();
      return out;
    } else {
      session.setDataPostion(dataPosition + Crypto.CHUNKSIZE);
      Progress.update((float) file.size, (float) session.getDataPostion());
      return null;
    }
  }

  // Input: Object:
  //	{
  //		name: File name,
  //		size: File size,
  //		data: Encrypted file (ArrayBuffer),
  //	}
  // myMiniLockID: Sender's miniLock ID (String)
  // mySecretKey: Sender's secret key (Uint8Array)
  // callback: Name of the callback function to which decrypted result is passed.
  // Result: Sends file to be decrypted, with the result picked up
  //	and sent to the specified callback.
  public static CallBackOnComplete decryptFile(
      MinilockFile file,
      String myMiniLockID,
      byte[] mySecretKey, MinilockFileReader fileReader)
      throws MiniLockException, UnsupportedEncodingException, StreamNaclException {

    DecryptionSession session = new DecryptionSession();
    int headerLength = Util.byteArrayToNumber(fileReader.read(8, 12));
    String headerString = null;
    headerString = new String(fileReader.read(12, headerLength + 12), "utf-8");
    Gson g = new GsonBuilder().disableHtmlEscaping().create();
    Header header;
    try {
      header = g.fromJson(headerString, Header.class);
      if (header.version == null || header.version != 1) {
        throw new MiniLockException(INVALID_HEADER_VERSION);
      }
      if (header.ephemeral == null || !Util.validateEphemeral(header.ephemeral)) {
        throw new MiniLockException(COULD_NOT_PARSE_HEADER);
      }

    } catch (JsonSyntaxException e) {
      throw new MiniLockException(COULD_NOT_PARSE_HEADER);
    }

    // Attempt decryptInfo decryptions until one succeeds
    DecryptInfo actualDecryptInfo = null;
    byte[] actualDecryptInfoNonce = null;
    FileInfo actualFileInfo = null;

    for (String entry : header.decryptInfo.keySet()) {
      if (Util.validateNonce(entry, 24)) {
        Box box = new Box(Base64.decode(header.ephemeral, 0), mySecretKey);
        byte[] decryptInfoNonce = Base64.decode(entry, 0);
        byte[] decryptInfo = box.open(Base64.decode(header.decryptInfo.get(entry), 0),
            decryptInfoNonce);

        if (decryptInfo != null) {
          actualDecryptInfo = g.fromJson(new String(decryptInfo, "utf-8"), DecryptInfo.class);


          actualDecryptInfoNonce = decryptInfoNonce;
          break;
        }
      }
    }


    if (actualDecryptInfo == null || actualDecryptInfo.recipientID == null
        || !actualDecryptInfo.recipientID.equals(myMiniLockID)) {
      throw new MiniLockException(NOT_ENCRYPTED_FOR_THIS_RECIPIENT);
    }

    if (actualDecryptInfo.fileInfo == null || actualDecryptInfo.senderID == null
        || !Util.validateID(actualDecryptInfo.senderID)) {
      throw new MiniLockException(COULDNT_VALIDATE_SENDER_ID);
    }
    try {
      Box box = new Box(
          Uint8Array.subarray(Base58.decode(actualDecryptInfo.senderID), 0, 32),
          mySecretKey);
      byte[] actualFileInfoBytes = box.open(Base64.decode(actualDecryptInfo.fileInfo, 0),
          actualDecryptInfoNonce);

      actualFileInfo = g.fromJson(
          new String(actualFileInfoBytes, "utf-8"),
          FileInfo.class
      );
    } catch (Exception e) {
      throw new MiniLockException(COULD_NOT_PARSE_HEADER);
    }
    // Begin actual ciphertext decryption
    Integer dataPosition = 12 + headerLength;
    session.setStreamDecryptor(new StreamDecryptor(
        Base64.decode(actualFileInfo.fileKey, 0),
        Base64.decode(actualFileInfo.fileNonce, 0),
        CHUNKSIZE
    ));
    session.setDataPostion(dataPosition);
    CallBackOnComplete callBackOnComplete = null;
    DecryptionFileWriter decryptionFileWriter = new DecryptionFileWriter();
    while (callBackOnComplete == null) {
      Log.d(TAG, "decryptFile: dataposition == " + dataPosition);
      callBackOnComplete = decryptNextChunk(
          file,
          actualFileInfo,
          actualDecryptInfo.senderID,
          headerLength,
          session,
          decryptionFileWriter,
          fileReader
      );
    }
    return callBackOnComplete;
  }

  //	Input:
  //		Entire file object,
  //		data position on which to start decryption (number),
  //		fileInfo object (From header),
  //		sender ID (Base58 string),
  //		header length (in bytes) (number),
  //		Callback to execute when last chunk has been decrypted.
  //	Result: Will recursively decrypt until the last chunk,
  //		at which point callbackOnComplete() is called.
  //		Callback is passed these parameters:
  //			file: Decrypted file object (blob),
  //			saveName: File name for saving the file (String),
  //			senderID: Sender's miniLock ID (Base58 string)
  public static CallBackOnComplete decryptNextChunk(
      MinilockFile file,
      FileInfo fileInfo,
      String senderID,
      int headerLength,
      DecryptionSession session,
      DecryptionFileWriter decryptionFileWriter, MinilockFileReader fileReader) throws MiniLockException, StreamNaclException {
    int dataPosition = session.getDataPostion();
    byte[] chunk = fileReader.read(
        dataPosition,
        dataPosition + 4 + 16 + CHUNKSIZE
    );
    int actualChunkLength = Util.byteArrayToNumber(
        Uint8Array.subarray(chunk, 0, 4)
    );
    if (actualChunkLength > chunk.length) {
      throw new MiniLockException(GENERAL_DECRYPTION_ERROR);
    }
    chunk = Uint8Array.subarray(chunk,
        0, actualChunkLength + 4 + 16
    );

    byte[] decryptedChunk;
    boolean isLast = false;
    if (
        dataPosition >= ((file.size) - (4 + 16 + actualChunkLength))
        ) {
      isLast = true;
    }
    if (dataPosition == (12 + headerLength)) {
      // This is the first chunk, containing the filename
      decryptedChunk = session.getStreamDecryptor().decryptChunk(
          chunk,
          isLast
      );

      if (decryptedChunk == null) {
        throw new MiniLockException(GENERAL_DECRYPTION_ERROR);
      }
      String fileName = new String(Uint8Array.subarray(decryptedChunk, 0, 256));
      while (
          fileName.charAt(fileName.length() - 1) == 0
        // TODO: 27/9/15 check that. String.copyValueOf() new String(fromCharCode(0x00)
          ) {
        fileName = fileName.substring(0, fileName.length() - 1);
      }
      if (MinilockFile.fileExist(fileName)) {
        throw new MiniLockException(MiniLockException.FILE_ALREADY_EXISTS);
      }
      session.setOutPutFileName(fileName);
      decryptionFileWriter.setFileName(fileName);
      session.getHashObject().update(Uint8Array.subarray(chunk, 0, 256 + 4 + 16));
    } else {
      decryptedChunk = session.getStreamDecryptor().decryptChunk(
          chunk,
          isLast
      );

      if (decryptedChunk == null) {
        throw new MiniLockException(GENERAL_DECRYPTION_ERROR);
      }
      decryptionFileWriter.add(decryptedChunk);


//        miniLock.UI.animateProgressBar(
//            dataPosition + actualChunkLength,
//            file.size - 12 - headerLength
//        )
      session.getHashObject().update(chunk);
    }

    session.setDataPostion(dataPosition + chunk.length);
    if (isLast) {
      Log.d(TAG, "Checking hash decryption");
      if (

          TweetNaclFast.crypto_verify_32(session.getHashObject().digest(),
              Base64.decode(fileInfo.fileHash, 0)
          ) != 0
          ) {
        throw new MiniLockException(COULDNT_VALIDATE_FILE_DECRYPTION);
      } else {
        session.getStreamDecryptor().clean();
        return new CallBackOnComplete(senderID, session.getOutPutFileName());

      }
    } else {
      return null;
    }

  }


  public static class CallBackOnComplete {
    public String myMinilockId;
    public String saveName;

    public CallBackOnComplete() {
    }

    public CallBackOnComplete(String myMinilockId, String saveName) {
      this.myMinilockId = myMinilockId;
      this.saveName = saveName;
    }
  }


//      dataPosition += chunk.length
//      if (isLast) {
//        if (
//            !nacl.verify(
//                new Uint8Array(miniLock.session.currentFile.hashObject.digest()),
//                nacl.util.decodeBase64(fileInfo.fileHash)
//            )
//            ) {
//          miniLock.UI.fileOperationHasFailed('decrypt', 7)
//          throw new Error('miniLock: Decryption failed - could not validate file contents after
// decryption')
//          return false
//        }
//        else {
//          miniLock.session.currentFile.streamDecryptor.clean()
//          return callbackOnComplete(
//              new Blob(miniLock.session.currentFile.decryptedChunks),
//              miniLock.session.currentFile.fileName,
//              senderID
//          )
//        }
//      }
//      else {
//        return miniLock.crypto.decryptNextChunk(
//            file,
//            dataPosition,
//            fileInfo,
//            senderID,
//            headerLength,
//            callbackOnComplete
//        )
//      }
//    }
//    )
//  }


}
