package com.uliamar.minilock.minilocklib;

import java.util.HashMap;

public class Header {
  Integer version;
  String ephemeral;
  HashMap<String, String> decryptInfo;

  public Header(int version, String ephemeral, HashMap<String, String> decryptInfo) {
    this.version = version;
    this.ephemeral = ephemeral;
    this.decryptInfo = decryptInfo;
  }

  //  version: 1
}
