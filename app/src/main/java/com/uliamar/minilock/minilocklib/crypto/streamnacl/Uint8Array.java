package com.uliamar.minilock.minilocklib.crypto.streamnacl;

import java.util.Arrays;

public class Uint8Array {

  public static byte[] set(byte[]dest, byte[] array) {
    return set(dest, array, 0);
  }

  public static byte[] set(byte[] dest, byte[] array, int offset) {
    System.arraycopy(array, 0, dest, offset, array.length);
    return dest;
  }

  public static byte[] subarray(byte[] original, int begin, int end) {
    return Arrays.copyOfRange(original, begin, end);
  }

}

