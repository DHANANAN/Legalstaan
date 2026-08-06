# Legalstaan Keystore & OAuth 2.0 Fingerprint Documentation (v1.35)

This document details all release key options and Google Cloud / Firebase OAuth configuration steps for Legalstaan v1.35 (`versionCode 35`).

---

## 1. Keystore Inventory

### Primary Dedicated v1.35 Keystore (`app/release-key-v35.jks`)
- **Keystore File:** `app/release-key-v35.jks`
- **Key Algorithm:** 4096-bit RSA
- **Alias:** `release-key-v35`
- **Store Password:** `password`
- **Key Password:** `password`
- **SHA-1 Fingerprint:** `A6:4A:92:D5:3B:39:6B:E8:07:9C:DF:2C:65:0B:35:08:41:8D:F3:C7`
- **SHA-256 Fingerprint:** `C4:C4:EC:61:A9:A1:D1:D8:2C:9B:F1:A4:84:9B:C7:DF:6E:ED:AE:E4:A1:C4:E8:4C:DF:63:E8:56:46:A5:D1:B4`

### Legacy Release Keystore (`app/release-key.jks`)
- **Keystore File:** `app/release-key.jks`
- **Alias:** `release-key`
- **SHA-1 Fingerprint:** `A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD`
- **SHA-256 Fingerprint:** `0C:6C:02:DD:AA:28:32:93:F8:05:BA:73:29:6D:E1:4D:08:27:26:45:56:64:58:6D:C2:C9:49:8B:8A:68:51:A8`

---

## 2. Firebase & Google Cloud Console Registration

### Firebase Project Target
- **Project ID:** `legalstaan`
- **Project Number:** `1055408756496`
- **Package Name:** `com.legalstaan.app`

### Step-by-Step Registration Guide

1. **Add Fingerprints to Firebase:**
   - Go to [Firebase Console](https://console.firebase.google.com/) -> **legalstaan** project.
   - Click ⚙ **Project Settings** -> **Your Apps** (`com.legalstaan.app`).
   - Click **Add Fingerprint** and paste:
     - SHA-1: `A6:4A:92:D5:3B:39:6B:E8:07:9C:DF:2C:65:0B:35:08:41:8D:F3:C7`
     - SHA-256: `C4:C4:EC:61:A9:A1:D1:D8:2C:9B:F1:A4:84:9B:C7:DF:6E:ED:AE:E4:A1:C4:E8:4C:DF:63:E8:56:46:A5:D1:B4`
   - Download the updated `google-services.json` and save to `app/google-services.json`.

2. **Resolving Duplicate SHA-1 Warnings:**
   - If Google Cloud Console flags SHA-1 as duplicate across projects:
     - Prefer registering the SHA-256 fingerprint in Firebase and Google Cloud OAuth client settings.
     - Delete any abandoned or test Android apps in older Firebase projects sharing `com.legalstaan.app`.

3. **Google Drive API Key Restrictions:**
   - Open [Google Cloud Console](https://console.cloud.google.com/) -> **APIs & Services** -> **Credentials**.
   - Select your Drive API key (`drive_api_key`).
   - Set **Application Restrictions** to **Android apps**.
   - Add item:
     - Package Name: `com.legalstaan.app`
     - SHA-1: `A6:4A:92:D5:3B:39:6B:E8:07:9C:DF:2C:65:0B:35:08:41:8D:F3:C7`
   - Set **API Restrictions** -> Restrict key -> Select **Google Drive API**.

---

## 3. Verification Commands

To re-verify keystore fingerprints via `keytool`:

```powershell
keytool -list -v -keystore app/release-key-v35.jks -alias release-key-v35 -storepass password
```
