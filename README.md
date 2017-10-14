# Android Crypto Tester

Android application for testing AES speed of the device

I take no responsibility for any damage this app may cause to your device. I have run it on 15+ devices and it went fine, but well...

# Background

This app was created as a proof that there is a huge performance drop in AES encryption & decryption on Android, introduced in version 24, which still exists in version 26.

# What does it do

This app will basically try to encrypt and decrypt small (200KB) and large (10MB) files on your device. Afterwards it will provide some statistics regarding the encryption and decryption speed.

# Why am I asked for WRITE_EXTERNAL_STORAGE permission?

This app will test both internal and external storage encryption/decryption.

# What's up with Firebase?

Internally we gather the stats using Firebase Analytics. I have commented out all of the Firebase calls, so you don't have to worry about any spying.

# How do I test it?

By default app is configured to use Android 23. Once built and tested, gather the results and switch to branch 26. Build the app, run it and compare results.

# Results

A sample of my results using Lenovo Yoga Tab 3 Pro (mouthful)

Android 23 large files decryption: 136959 B/s
Android 26 large files decryption: 34419 B/s
