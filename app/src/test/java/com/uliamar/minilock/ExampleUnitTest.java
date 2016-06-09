package com.uliamar.minilock;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() throws Exception {
    assertEquals(4, 2 + 2);
  }




//
//
////    setContentView(R.layout.activity_main);
//    new AsyncTask<Void, Void, Void>() {
//
//      @Override
//      protected Void doInBackground(Void... voids) {
////        File fileStorageDir = FileUtils.getFileStorageDir();
////        Log.d(TAG, "file exist = " + fileStorageDir.exists());
//        MinilockFile fileToOpen = new MinilockFile("crypted.txt", 31l, new byte[1]);
////        byte[] readed = MinilockFile.read(fileToOpen, 0, Crypto.CHUNKSIZE);
//        String myMiniLockID = "qHd9fQ8QW5DYZqoL66ZDza2hUBkSFUTu7SFsBrj9JvuAT";
//        byte[] mySecretKey;
//
////        try {
////          mySecretKey = "".getBytes("utf-8");
////
////        } catch (UnsupportedEncodingException e) {
////          throw new RuntimeException("utf non géré.");
////        }
//        mySecretKey = new byte[]{(byte) (190 & 0xFF), (byte) (69 & 0xFF), (byte) (137 & 0xFF), (byte) (248 & 0xFF), (byte) (149 & 0xFF), (byte) (244 & 0xFF), (byte) (250 & 0xFF), (byte) (213 & 0xFF), (byte) (249 & 0xFF), (byte) (14 & 0xFF), (byte) (124 & 0xFF), (byte) (65 & 0xFF), (byte) (209 & 0xFF), (byte) (193 & 0xFF), (byte) (191 & 0xFF), (byte) (253 & 0xFF), (byte) (76 & 0xFF), (byte) (238 & 0xFF), (byte) (146 & 0xFF), (byte) (202 & 0xFF), (byte) (189 & 0xFF), (byte) (219 & 0xFF), (byte) (249 & 0xFF), (byte) (101 & 0xFF), (byte) (23 & 0xFF), (byte) (109 & 0xFF), (byte) (161 & 0xFF), (byte) (144 & 0xFF), (byte) (207 & 0xFF), (byte) (65 & 0xFF), (byte) (150 & 0xFF), (byte) (145 & 0xFF)};
//        String[] destinataire = new String[]{myMiniLockID};
//        try {
//          Crypto.CallBackOnComplete callBackOnComplete = Crypto.encryptFile(fileToOpen, "crypted" +
//              ".txt", destinataire, myMiniLockID, mySecretKey, new
//              Crypto.Callback() {
//
//
//                @Override
//                public void callBack(byte[] key) {
//                  Log.d(TAG, "On est dans le callbak et je ne sais trop quoi dire");
//                }
//              });
//
//
//        } catch (StreamNaclBase.StreamNaclException e) {
//          e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//          e.printStackTrace();
//        }
//        return null;
//      }
//    };
//
//    new AsyncTask<Void, Void, Void>() {
//
//      @Override
//      protected Void doInBackground(Void... voids) {
//        MinilockFile fileToOpen = new MinilockFile("crypted.txt.minilock", 973l, new byte[1]);
////        byte[] readed = MinilockFile.read(fileToOpen, 0, Crypto.CHUNKSIZE);
//        String myMiniLockID = "qHd9fQ8QW5DYZqoL66ZDza2hUBkSFUTu7SFsBrj9JvuAT";
//        byte[] mySecretKey;
//        mySecretKey = new byte[]{(byte) (190 & 0xFF), (byte) (69 & 0xFF), (byte) (137 & 0xFF), (byte) (248 & 0xFF), (byte) (149 & 0xFF), (byte) (244 & 0xFF), (byte) (250 & 0xFF), (byte) (213 & 0xFF), (byte) (249 & 0xFF), (byte) (14 & 0xFF), (byte) (124 & 0xFF), (byte) (65 & 0xFF), (byte) (209 & 0xFF), (byte) (193 & 0xFF), (byte) (191 & 0xFF), (byte) (253 & 0xFF), (byte) (76 & 0xFF), (byte) (238 & 0xFF), (byte) (146 & 0xFF), (byte) (202 & 0xFF), (byte) (189 & 0xFF), (byte) (219 & 0xFF), (byte) (249 & 0xFF), (byte) (101 & 0xFF), (byte) (23 & 0xFF), (byte) (109 & 0xFF), (byte) (161 & 0xFF), (byte) (144 & 0xFF), (byte) (207 & 0xFF), (byte) (65 & 0xFF), (byte) (150 & 0xFF), (byte) (145 & 0xFF)};
//        String[] destinataire = new String[]{myMiniLockID};
//        try {
//          Crypto.decryptFile(fileToOpen, myMiniLockID, mySecretKey, new
//              Crypto.CallBackOnComplete() {
//
//
//                public void callBack(byte[] key) {
//                  Log.d(TAG, "On est dans le callbak et je ne sais trop quoi dire");
//                }
//              });
//        } catch (StreamNaclBase.StreamNaclException e) {
//          e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//          e.printStackTrace();
//        } catch (Exception e) {
//          e.printStackTrace();
//        }
//        return null;
//      }
//
//    };
//
//
//    new AsyncTask<Void, Void, Void>() {
//
//      @Override
//      protected Void doInBackground(Void... params) {
//        String passphrase = "This passphrase is supposed to be good enough for miniLock. :-)";
//          TweetNaclFast.Box.KeyPair keyPair;
//        try {
//          Log.d(TAG, "in da place");
//          keyPair = Crypto.getKeyPair(passphrase, "miniLockScrypt..");
//          String publicKey = Base58.encode(keyPair.getPublicKey());
//          if (!publicKey.equals("EWVHJniXUFNBC9RmXe45c8bqgiAEDoL3Qojy2hKt4c4e")) throw new AssertionError();
//          Log.d(TAG, "public key  passed");
//          String privateKey = Base64.encodeToString(keyPair.getSecretKey(), Base64.NO_WRAP);
//          if (!privateKey.equals("6rcsdGAhF2rIltBRL+gwvQTQT7JMyei/d2JDrWoo0yw=")) throw new AssertionError();
//          Log.d(TAG, "private key  passed");
//          String miniLockID = Crypto.getMiniLockID(keyPair.getPublicKey());
//          if (!miniLockID.equals("22d9pyWnHVGQTzCCKYEYbL4YmtGfjMVV3e5JeJUzLNum8A")) throw new AssertionError();
//          Log.d(TAG, "minilockId key  passed");
//
//        } catch (Exception e) {
//          Log.e(TAG, "An error happened");
//          e.printStackTrace();
//        }
//        return null;
//      }
//    };







//  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//    // Inflate the menu; this adds items to the action bar if it is present.
//    getMenuInflater().inflate(R.menu.menu_main, menu);
//    return true;
//  }
//
//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//    // Handle action bar item clicks here. The action bar will
//    // automatically handle clicks on the Home/Up button, so long
//    // as you specify a parent activity in AndroidManifest.xml.
//    int id = item.getItemId();
//
//    //noinspection SimplifiableIfStatement
//    if (id == R.id.action_settings) {
//      return true;
//    }
//
//    return super.onOptionsItemSelected(item);
//  }
}