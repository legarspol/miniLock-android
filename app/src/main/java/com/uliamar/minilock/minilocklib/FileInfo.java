package com.uliamar.minilock.minilocklib;

public class FileInfo {
  String fileKey;
  String fileNonce;
  String fileHash;

  public FileInfo(String fileKey, String fileNonce, String fileHash) {
    this.fileKey = fileKey;
    this.fileNonce = fileNonce;
    this.fileHash = fileHash;
  }
}
