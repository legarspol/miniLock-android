package com.uliamar.minilock.minilocklib;

public class DecryptInfo {

//  senderID: senderID,
//  recipientID: miniLockIDs[i],
//  fileInfo: {
//    fileKey: nacl.util.encodeBase64(fileKey),
//        fileNonce: nacl.util.encodeBase64(fileNonce),
//        fileHash: nacl.util.encodeBase64(
//        miniLock.session.currentFile.hashObject.digest()
//    )
//  }
//
  String senderID;
  String recipientID;
  String fileInfo;

  public DecryptInfo(String myMiniLockID, String recipientID) {
    this.senderID = myMiniLockID;
    this.recipientID = recipientID;
  }
}
