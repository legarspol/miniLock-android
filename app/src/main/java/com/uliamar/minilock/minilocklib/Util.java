package com.uliamar.minilock.minilocklib;

import com.uliamar.minilock.minilocklib.crypto.Base58;
import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;
import com.uliamar.minilock.minilocklib.crypto.streamnacl.Uint8Array;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Util {
  static final Pattern base58Match = Pattern.compile
      ("^[1-9ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]+$");
  static final Pattern base64Match = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*" +
      "(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");


//  // Input: none
//// Result: Resets miniLock.session.currentFile
//  miniLock.util.resetCurrentFile = function() {
//    delete miniLock.session.currentFile
//    miniLock.session.currentFile = {
//        fileObject: null,
//        fileName: '',
//        encryptedChunks: [],
//    decryptedChunks: [],
//    hashObject: new BLAKE2s(32),
//        streamEncryptor: null,
//        streamDecryptor: null
//    }
//  }
//


  // Input: String
  // Output: Boolean
  // Notes: Validates if string is a proper miniLock ID.
  public static boolean validateID(String id) {

    if ((id.length() > 55) || (id.length() < 40)) {
      return false;
    }
    if (!base58Match.matcher(id).matches()) {
      return false;
    }
    byte[] bytes = Base58.decode(id);
    if (bytes.length != 33) {
      return false;
    }
    Blake2s hash = new Blake2s(1, null);
    hash.update(Uint8Array.subarray(bytes, 0, 32));
    if (hash.digest()[0] != bytes[32]) {
      return false;
    }
    return true;
  }


  // Input: Nonce (Base64) (String), Expected nonce length in bytes (Number)
  // Output: Boolean
  // Notes: Validates if string is a proper nonce.
  public static boolean validateNonce(String nonce, int expectedLength) {

    if ((nonce.length() > 40) || (nonce.length() < 10)) {
      return false;
    }
    if (base64Match.matcher(nonce).matches()) {
      byte[] bytes = TweetNaclFast.base64Decode(nonce);
      return bytes.length == expectedLength;
    }
    return false;
  }


  // Input: String
  // Output: Boolean
  // Notes: Validates if string is a proper symmetric key.
  public static boolean validateKey(String key) {

    if ((key.length() > 50) || (key.length() < 40)) {
      return false;
    }
    if (base64Match.matcher(key).matches()) {
      byte[] bytes = TweetNaclFast.base64Decode(key);
      return bytes.length == 32;
    }
    return false;
  }

  public static boolean validateEphemeral(String key) {
    return validateKey(key);
  }

  // Input: none
  // Output: Random string suitable for use as filename.
  public static String getRandomFilename() {
    byte[] randomBytes = TweetNaclFast.randombytes(6);
    return Base58.encode(randomBytes);
  }

  // Input: Filename (String)
  // Output: Whether filename extension looks suspicious (Boolean)
  public static boolean isFilenameSuspicious(String filename) {
    String[] suspicious = {
        "exe", "scr", "url", "com", "pif", "bat",
        "xht", "htm", "html", "xml", "xhtml", "js",
        "sh", "svg", "gadget", "msi", "msp", "hta",
        "cpl", "msc", "jar", "cmd", "vb", "vbs",
        "jse", "ws", "wsf", "wsc", "wsh", "ps1",
        "ps2", "ps1xml", "ps2xml", "psc1", "scf", "lnk",
        "inf", "reg", "doc", "xls", "ppt", "pdf",
        "swf", "fla", "docm", "dotm", "xlsm", "xltm",
        "xlam", "pptm", "potm", "ppam", "ppsm", "sldm",
        "dll", "dllx", "rar", "zip", "7z", "gzip",
        "gzip2", "tar", "fon", "svgz", "jnlp"
    };
    Pattern extensionMatch = Pattern.compile("\\.\\w+$");
    String extension;
    try {

      extension = extensionMatch.matcher(filename.toLowerCase()).group(1);

    } catch (IllegalStateException e) {
      return true;
    }


    extension = extension.substring(1);
    return (Arrays.asList(suspicious).contains(extension));
  }


  // Input: 4-byte little-endian Uint8Array
  // Output: ByteArray converter to number
  public static int byteArrayToNumber(byte[] byteArray) {
    int n = 0;
    for (int i = 3; i >= 0; i--) {
      n += byteArray[i]&0xFF;
      if (i > 0) {
        n = n << 8;
      }
    }
    return n;
  }

  // Input: Number
  // Output: Number as 4-byte little-endian Uint8Array
  public static byte[] numberToByteArray(int n) {
    byte[] byteArray = {0, 0, 0, 0};
    for (int i = 0; i < byteArray.length; i++) {
      byteArray[i] = (byte)(n & 0xFF);
      n = n >> 8;
    }
    return byteArray;
  }

}
