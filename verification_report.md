# Legalstaan v1.35 Verification & Release Report

**Target Branch:** `release/v1.35`  
**Repository:** `https://github.com/DHANANAN/Legalstaan.git`  
**Target Version:** `versionCode 35`, `versionName "1.35"`  
**Verification Date:** August 6, 2026

---

## 1. Acceptance Criteria Status

| Criteria | Result | Notes |
|---|---|---|
| **Zero 404 Links in config.json** | PASS | 13 deleted 404 links removed |
| **JSON Syntax & Validation** | PASS | Validated with `python -c "import json..."` |
| **Drive Fallback Logic** | PASS | `SubjectVideosActivity.java` falls back to static items on Drive error |
| **Build Success (assembleRelease)** | PASS | `legalstaan-v1.35-release.apk` generated (6.26 MB) |
| **Build Success (bundleRelease)** | PASS | `legalstaan-v1.35-release.aab` generated (9.76 MB) |
| **Keystore Fingerprints Recorded** | PASS | Documented in `KEYSTORE_AND_OAUTH_DOC.md` |
| **Git Branch & Push** | PASS | Branch `release/v1.35` created and pushed |

---

## 2. Release Artifacts

| Artifact | File Path | Size | Description |
|---|---|---|---|
| **Signed Release APK** | `apk/legalstaan-v1.35-release.apk` | 6.26 MB | Production release APK signed with `release-key.jks` |
| **Signed Release AAB** | `apk/legalstaan-v1.35-release.aab` | 9.76 MB | Production Play Store App Bundle signed with `release-key.jks` |

---

## 3. Keystore Certificate Fingerprints

- **Keystore:** `app/release-key.jks`
- **Alias:** `release-key`
- **SHA-1:** `A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD`
- **SHA-256:** `0C:6C:02:DD:AA:28:32:93:F8:05:BA:73:29:6D:E1:4D:08:27:26:45:56:64:58:6D:C2:C9:49:8B:8A:68:51:A8`
