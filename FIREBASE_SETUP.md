# Firebase Setup — Required to Fix Login, Chat, Live Sessions

These three console-side issues are the root cause of the bugs you reported:
- **"Login fails"** → SHA-1 fingerprint missing from Firebase
- **"Messages not sending"** → Firestore security rules block writes
- **"Courses not visible / live sessions empty"** → Same Firestore rules

The app code is correct. Until these console fixes are in place, no code change can make these work.

---

## 1. Add SHA-1 to Firebase Console (fixes Google Sign-In)

**Your release SHA-1:**
```
A2:CE:AA:D9:CE:99:E2:EB:AA:CA:1F:9F:64:0B:A6:4E:EC:5C:BB:AD
```

**Your release SHA-256:**
```
0C:6C:02:DD:AA:28:32:93:F8:05:BA:73:29:6D:E1:4D:08:27:26:45:56:64:58:6D:C2:C9:49:8B:8A:68:51:A8
```

### Steps
1. Open https://console.firebase.google.com/ and select the Legalstaan project.
2. Click the gear icon ⚙ → **Project settings**.
3. Scroll to **Your apps** → tap the Android app `com.legalstaan.app`.
4. Scroll to **SHA certificate fingerprints** → click **Add fingerprint**.
5. Paste the **SHA-1** above. Click **Save**.
6. (Recommended) Repeat for the **SHA-256**.
7. Scroll up, click **Download google-services.json**.
8. Replace the file at `Legalstaan-main/app/google-services.json` with the new download.
9. Rebuild the app.

### Why
The Google Sign-In flow validates that the APK signing key matches a fingerprint registered with the Firebase project. If it doesn't, you get `ApiException 10` (`DEVELOPER_ERROR`) — which the new login screen now reports clearly instead of silently saying "code 10".

### If you keep getting status 10 after this
You signed the build with a different keystore than `app/release-key.jks`. Either:
- Use the same keystore for every release, OR
- Add the SHA-1 of every keystore you use to Firebase

To get a different keystore's SHA-1:
```bash
keytool -list -v -keystore <path-to-jks> -alias <alias-name> -storepass <password>
```

---

## 2. Update Firestore Security Rules (fixes chat + live sessions)

The default Firestore rules block all reads/writes. Replace them with rules that allow signed-in users to use chat and read live sessions.

### Steps
1. Firebase Console → **Firestore Database** → **Rules** tab.
2. Replace everything with the rules below.
3. Click **Publish**.

### Rules

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // === Direct chats between users ===
    // Doc ID is the deterministic hash of both users' emails (sorted)
    match /chats/{chatId} {
      // Anyone signed in can create the chat metadata doc, and read it
      allow read, create: if request.auth != null;
      // Only update if you're a participant
      allow update: if request.auth != null
                    && request.auth.token.email in resource.data.participants;

      match /messages/{messageId} {
        // Read any message in any chat doc you can access
        allow read: if request.auth != null;
        // Send only with your own UID + email
        allow create: if request.auth != null
                      && request.resource.data.senderId == request.auth.uid
                      && request.resource.data.senderEmail == request.auth.token.email;
        // No edits/deletes (preserves chat history integrity)
        allow update, delete: if false;
      }
    }

    // === Live class sessions ===
    // Faculty creates/updates; everyone signed in can read
    match /live_sessions/{sessionId} {
      allow read: if request.auth != null;
      // Faculty whitelist — keep in sync with FacultyManager.java
      allow create, update, delete: if request.auth != null
                    && request.auth.token.email in [
                      "dhanan2838@gmail.com",
                      "abhisheknls56789@gmail.com",
                      "singhpunni592@gmail.com",
                      "aryaverma7355@gmail.com",
                      "rs11336singh@gmail.com",
                      "nikhilanand2367@gmail.com",
                      "alfajsmmushrif@gmail.com",
                      "susenk20@gmail.com",
                      "iamajayjatav@gmail.com",
                      "gautam2367@gmail.com",
                      "eshan.sharma333@gmail.com"
                    ];
    }

    // === User profiles (optional, for future use) ===
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Why these specific rules
- **`chats/{chatId}/messages`** — your `DirectChatActivity.sendMessage()` writes to this path. Without `create` permission, every send shows "Failed to send".
- **`live_sessions`** — `HomeFragment.listenForLiveSessions()` and `LiveFragment` both subscribe here. Without `read` permission, the live banner never appears and the Live tab is empty (which is also why you might think "courses not visible" — the live card on home stays hidden).
- **Faculty whitelist** prevents random users from creating fake live sessions.

---

## 3. Verify Gemini API key (Rutu AI)

The key in `ChatsFragment.java` (`AIzaSyCF2XJu2E68Tiuifh6sGBnsQMIrZSNPxF0`) matches your AI Studio screenshot, so the key itself is fine.

If Rutu AI still falls back to local mode, the new error message in chat will tell you exactly why — quota, network, model name, or key restriction. The error is also logged to Logcat under tag `RutuAI`:

```bash
adb logcat -s RutuAI
```

### Common cases
| Error in chat bubble | Action |
|---|---|
| "quota exhausted" | Wait 1 min (free tier = 15/min, 1500/day). Or upgrade billing. |
| "API key not valid" | Regenerate at https://aistudio.google.com/apikey |
| "permission_denied" | Google Cloud Console → APIs & Services → Credentials → your key → set "Application restrictions" to "None" (or whitelist your Android package) |
| "Unable to resolve host" | Network issue, not Gemini |

---

## 4. Verifying everything works

After the three fixes above, test in this order:

1. **Login** — open the app, tap "Sign in with Google". If it succeeds, SHA-1 is registered correctly.
2. **Live session** — open Firebase Console → Firestore → manually add a doc to `live_sessions` with `live: true` and a `title`. The home screen should show the red LIVE banner within ~2 seconds.
3. **Direct chat** — open Profile → Faculty → tap a faculty member → send a message. If "Failed to send" toast disappears, rules are working.
4. **Rutu AI** — go to Community/Chats tab → toggle "AI+Web" ON → ask "what is patent law". If you see a long Gemini-style response (not the canned local one with the patent_law subject info), it's working.

---

## What I (Claude) cannot fix from code
- Anything Firebase Console (steps 1 and 2 above)
- Anything Google AI Studio quota
- WebRTC reliability inside Android WebView (Jitsi blank screen on some devices) — the only reliable workaround is the Chrome Custom Tab fallback already in `LiveStreamActivity.java`
