# Drive folder integration — setup guide

Legalstaan can pull lectures and study material **live** from Google Drive
folders, so faculty just upload files to a folder and they appear in the app
without a release. This guide walks through the one-time setup.

## 1. Get a Drive API key

1. Open [Google Cloud Console](https://console.cloud.google.com/) and select
   the project you use for Firebase / Sign-In (or create a new one).
2. **APIs & Services → Library →** search for **Google Drive API** → **Enable**.
3. **APIs & Services → Credentials → Create credentials → API key**.
4. Click the new key, then **Edit API key**:
   - **Application restrictions:** Android apps
   - **Add an item:**
     - Package name: `com.legalstaan.app`
     - SHA-1: paste the same release SHA-1 you used for Firebase (see
       `FIREBASE_SETUP.md`).
   - **API restrictions:** Restrict key → tick **Google Drive API** only.
5. Copy the key.

## 2. Drop the key into the app

Open `app/src/main/res/values/strings.xml` and replace the placeholder:

```xml
<string name="drive_api_key" translatable="false">PASTE_DRIVE_API_KEY_HERE</string>
```

with the real key. Rebuild.

> **Don't commit a real key to a public fork.** API restrictions help, but the
> safest pattern is to keep the placeholder in git and only put the real key in
> your local copy when building a release APK.

## 3. Share your Drive folder

For each folder you want to expose:

1. Right-click the folder in Drive → **Share**.
2. Set **General access** to **Anyone with the link → Viewer**.
3. Copy the link — it looks like
   `https://drive.google.com/drive/folders/1AbCdEfGh_iJkLmNoPqRsTuVwXyZ`.
4. The folder ID is the part after `/folders/` —
   `1AbCdEfGh_iJkLmNoPqRsTuVwXyZ` in this example.

## 4. Wire the folder into the app

Edit `app/src/main/assets/config.json`. For each subject you want to power
from Drive, add a `folder_id` field:

```json
{
  "subjects": [
    {
      "id": "trademark_law",
      "title": "Trademark Law",
      "color": "#EF5350",
      "folder_id": "1AbCdEfGh_iJkLmNoPqRsTuVwXyZ"
    }
  ]
}
```

You can keep `videos` alongside `folder_id` as a fallback, but if `folder_id`
is set, the app fetches the folder live and ignores the static list.

The app classifies items by mime-type:

| Mime-type | Shown as | Tap behavior |
|---|---|---|
| `video/*` | **Lecture** | Plays in the in-app Drive video player |
| `application/pdf` | **Study Material** | Opens in Chrome Custom Tab |
| `application/vnd.google-apps.folder` | **Folder** | Drills in (recursive) |
| Google Docs / Sheets / Slides | (skipped) | — |

Sub-folders inside the linked folder are browsable — users can drill in and
back out naturally.

## 5. Common errors

The app will show one of these messages if Drive can't serve the folder:

| Message | Likely fix |
|---|---|
| "Drive API key not configured" | The placeholder is still in `strings.xml`. Paste your key. |
| "Access denied. Make sure the folder is shared 'Anyone with the link'…" | Folder is private, or the API key doesn't allow your app. |
| "Folder not found" | Wrong folder ID in `config.json`. |
| "Drive API daily quota exhausted" | Too many requests today — wait or raise quota in Cloud Console. |

## 6. Quotas

The free tier gives Drive API ~1 billion queries/day per project — far more
than this app will ever need. The pre-emptive concern is the per-100-second
limit (default 20,000 / 100 sec / user). Drive responses are cached by the
WebView for media playback, so listing requests are rare.
