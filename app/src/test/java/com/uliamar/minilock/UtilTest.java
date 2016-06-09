package com.uliamar.minilock;

import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;
import com.uliamar.minilock.minilocklib.Crypto;
import com.uliamar.minilock.minilocklib.Util;

import junit.framework.TestCase;

import java.util.regex.Pattern;

public class UtilTest extends TestCase {
  Pattern valideFileName = Pattern.compile("^\\w{6,12}$");

  public void testValidateID() throws Exception {
    String[] IDs = {
        Crypto.getMiniLockID(TweetNaclFast.Box.keyPair().getPublicKey()),
        Crypto.getMiniLockID(TweetNaclFast.Box.keyPair().getPublicKey()),
        Crypto.getMiniLockID(TweetNaclFast.Box.keyPair().getPublicKey()),
        Crypto.getMiniLockID(TweetNaclFast.Box.keyPair().getPublicKey())
    };
    String[] notIDs = {
        "clearly not an ID",
        "ejSSnXzCP806SWiDgeueYlwf2U8utLSkNhwU1VoiAE=",
        "7m+Saj2zhy0mGNKEJPI8V1kyFfZZfcxbWxCcYlwPF0=",
        "4153+icvEiQuiBpttMfHjUsMuy2vDCylzpnTSMQFY2SQ=",
        "u1F4OzpS9PO3ZQitXFDDwTdLsZmLlA3rCNwCYsnBjY=",
        "YWFhYWFhYWFhYWJiYmJiYmJiYmJjY2NjY2NjY2Nj"
    };
    for (int i = 0; i < IDs.length; i++) {
      assertTrue("Valid ID " + i, Util.validateID(IDs[i]));
    }
    for (int o = 0; o < notIDs.length; o++) {
      assertTrue("Invalid ID " + o, !Util.validateID(notIDs[o]));
    }
  }

  public void testIsFilenameSuspicious() {
    String[] suspicious = {
        "innocent.exe",
        "happy.jpg.bat",
        "totallySafe.jpg.exe.png.html",
        "noExtension"
    };
    String[] clean = {
        "photos.jpg",
        "my.files.js.jpeg"
    };
    for (int s = 0; s < suspicious.length; s++) {
      assertTrue("Suspicious filename " + s, Util.isFilenameSuspicious(suspicious[s]));

    }
    for (int c = 0; c < clean.length; c++) {
      assertTrue("Clean filename " + c, Util.isFilenameSuspicious(clean[c]));
    }
  }

  public void testGetRandomFilename() throws Exception {
    String filename = Util.getRandomFilename();
    assertTrue("Filename formatting", valideFileName.matcher(filename).matches());
  }
}