## miniLock for Android

This app is an unofficial port of [miniLock](https://minilock.io/) for Android.
 
You now can encrypt and decrypt files on the go with miniLock for Android. You'll never be stuck with a .miniLock on mobile anymore !
 
miniLock is an innovative cryptographic software which allows you to encrypt files and send it to your friend or keep it to yourself and only yourself.
Unlike many software, miniLock do not needs you to store and keep it's cryptographic key safe. The cryptographic key is derivated from your input on a login/password shape.



### Contributing
Any contribution is welcome either by filling bug in the github issue tracker or by picking one item in the todo :-)

#### Todo
- Remove fabric secret of android manifest
- Better notification (Display loading in full screen when user click on notification)
- Handle `Send to > miniLock` Action
- calculate the entropy of the inputed password with zxcvbn
- Phrase.secureRandom shoud be rewritten
- Make a lib of the core


#### Feature Idea
- Make the test of the key derivation run without calling it in the MainActivity
- Handle zip
- Have a crypted contact list in the app


#### How to build:

- Open the sources with Android studio
- in /app create a file fabric.properties thanks to the model in the same folder
- Press Play

This app uses @haochenx fork of wg/scrypt. The scrypt library is already compiled and included in the project. If you want to recompile them the current version of the app uses the commit `371d55c` which you can fetch [either on Hao Chen repo](https://github.com/haochenx/scrypt/tree/1.4.0-371d55) or [in the copy left in this repo](./Document/scrypt haochen.zip). Only 2 lines got edited: the one which yell the user's credential in the consol ;-) 


### Credits

This app is an Android adaptation of Nadim Kobeissi product. miniLock is a desktop software released under the AGPLv3 License. 
Check out:
 
 - [minilock's webiste](https://minilock.io/)
 - [miniLock's original codebase](https://github.com/kaepora/miniLock)
 - [Nadim Kobeissi's personal website](https://nadim.computer/)


This rewriting of the app has been written by legarspol and is available under the same AGPLv3 license

#### Scrypt

This app uses [the fork of haochenx](https://github.com/haochenx/scrypt/tree/1.4.0-371d55) of [Will Glozer's java SCrypt implementation](https://github.com/wg/scrypt)

#### TweetNacl

https://github.com/InstantWebP2P/tweetnacl-java Copyright(2014-2015) by tom zhou, iwebpp@gmail.com

#### Blake2s Java implementation

https://github.com/legarspol/java-blake2s

