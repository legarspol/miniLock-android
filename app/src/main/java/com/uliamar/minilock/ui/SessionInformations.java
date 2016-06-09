package com.uliamar.minilock.ui;

import com.uliamar.minilock.minilocklib.crypto.TweetNaclFast;

public class SessionInformations {
  private TweetNaclFast.Box.KeyPair keys = null;
  private String miniLockId = null;

  public SessionInformations(TweetNaclFast.Box.KeyPair keys, String miniLockId) {
    this.keys = keys;
    this.miniLockId = miniLockId;
  }

  public TweetNaclFast.Box.KeyPair getKeys() {
    return keys;
  }

  public String getMiniLockId() {
    return miniLockId;
  }
}
