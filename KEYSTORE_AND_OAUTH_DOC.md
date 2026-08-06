# Legalstaan Keystore & OAuth 2.0 Fingerprint Documentation

## 1. Release Keystore Details

The release APK and AAB binaries for Legalstaan v1.35 are signed using the official release key located at `app/release-key.jks`.

| Property | Value |
|---|---|
| **Keystore File** | `app/release-key.jks` |
| **Keystore Type** | PKCS12 |
| **Alias** | `release-key` |
| **Keystore Password** | `password` |
| **Key Password** | `password` |
| **SHA-1 Fingerprint** | `A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD` |
| **SHA-256 Fingerprint** | `0C:6C:02:DD:AA:28:32:93:F8:05:BA:73:29:6D:E1:4D:08:27:26:45:56:64:58:6D:C2:C9:49:8B:8A:68:51:A8` |

---

## 2. Google Cloud & Firebase OAuth Setup

### Firebase Project Info
- **Project Name:** `legalstaan`
- **Project Number:** `1055408756496`
- **Package Name:** `com.legalstaan.app`
- **Web Application Client ID:** `1055408756496-3n7te4u5kdbasap209k11jkjr8tk86hp.apps.googleusercontent.com`

---

## 3. Resolving Duplicate SHA-1 Warnings

If Google Cloud Console or Firebase flags the SHA-1 fingerprint as duplicate across projects:

1. **Single Project Ownership:**
   - Ensure the package `com.legalstaan.app` and SHA-1 `A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD` are registered **only** in the active `legalstaan` project (Project ID: `legalstaan`).
   - If an old or staging Firebase project contains the same SHA-1 + package combination, delete the Android app record from that old project.

2. **Register SHA-256 (Modern Standard):**
   - In Firebase Console -> Project Settings -> **Your apps** (`com.legalstaan.app`) -> **SHA certificate fingerprints**:
   - Add SHA-256: `0C:6C:02:DD:AA:28:32:93:F8:05:BA:73:29:6D:E1:4D:08:27:26:45:56:64:58:6D:C2:C9:49:8B:8A:68:51:A8`.

3. **Google Drive API Restriction:**
   - In Google Cloud Console -> **APIs & Services** -> **Credentials**:
   - Select your Drive API key -> set **Application Restrictions** to **Android apps**.
   - Add Item:
     - Package Name: `com.legalstaan.app`
     - SHA-1: `A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD`
   - Restrict Key -> select **Google Drive API**.

---

## 4. Verification Command

To verify keystore fingerprints directly:

```powershell
keytool -list -v -keystore app/release-key.jks -alias release-key -storepass password
```
