# Legalstaan Keystore & OAuth 2.0 Fingerprint Documentation

This document details the exact certificate fingerprints configured in the Legalstaan Firebase Console for version 1.35 (`versionCode 35`).

---

## 1. Active Release Keystore Details (`app/release-key.jks`)

All production release builds (`app-release.apk` and `app-release.aab`) are signed strictly with `app/release-key.jks`.

| Property | Value |
|---|---|
| **Keystore File** | `app/release-key.jks` |
| **Keystore Type** | PKCS12 |
| **Alias** | `release-key` |
| **Keystore Password** | `password` |
| **Key Password** | `password` |
| **Release SHA-1** | `A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD` |
| **Release SHA-256** | `0C:6C:02:DD:AA:28:32:93:F8:05:BA:73:29:6D:E1:4D:08:27:26:45:56:64:58:6D:C2:C9:49:8B:8A:68:51:A8` |

---

## 2. Firebase Console Registered Fingerprints

The following fingerprints are registered and active in Firebase Console for `com.legalstaan.app`:

### SHA-1 Fingerprints
1. `a2:ce:aa:d9:ce:99:e2:eb:aa:ca:1f:9f:64:0b:a6:4e:ec:5c:bb:ad` (Production Release Keystore)
2. `2a:cd:02:75:33:aa:26:6c:2b:ed:e5:a1:7e:fd:98:25:7f:98:d6:c0` (Debug Keystore)

*(Note: The duplicate/warning SHA-1 `e0:ec:c9:d7:00:a7...` with the yellow icon should be safely ignored or deleted from Firebase Console).*

### SHA-256 Fingerprints
1. `0c:6c:02:dd:aa:28:32:93:f8:05:ba:73:29:6d:e1:4d:08:27:26:45:56:64:58:6d:c2:c9:49:8b:8a:68:51:a8` (Production Release Keystore)

---

## 3. Verification Command

To verify `app/release-key.jks` fingerprints locally:

```powershell
keytool -list -v -keystore app/release-key.jks -alias release-key -storepass password
```
