package com.uliamar.minilock.minilocklib;

import java.security.SecureRandom;

public class Phrase {
  String[] words;

  public Phrase(String[] words) {
    this.words = words;
  }

  //  // Utility function: Secure function that returns a number in the range [0, count)
//  var secureRandom = function(count) {
//    var rand = new Uint32Array(1)
//    var skip = 0x7fffffff - 0x7fffffff % count
//    var result
//
//    if (((count - 1) & count) === 0) {
//      window.crypto.getRandomValues(rand)
//      return rand[0] & (count - 1)
//    }
//    do {
//      window.crypto.getRandomValues(rand)
//      result = rand[0] & 0x7fffffff
//    } while (result >= skip)
//    return result % count
//  }
  private static int secureRandom(int max) {
// TODO: 23/11/15 Make it secure
    SecureRandom sr = new SecureRandom();
    int i = sr.nextInt() % max;
    return i > 0 ? i : -i;
  }

  public String get(int length) {
    String out = "";
    for (int i = 0; i < length; ++i) {
      if (i != 0) {
        out += " ";
      }
      out += words[secureRandom(words.length)];
    }
    return out;
  }

}
