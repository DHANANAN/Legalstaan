# Legalstaan v1.30 — Play Console Publish Runbook

Everything in this folder is copy-paste ready. Follow this runbook top-to-bottom.

## 0. Files in this folder

| File | What it's for |
|---|---|
| `short_description.txt` | 140-char short description |
| `full_description.txt` | 118-word full description |
| `release_notes_v1.30.txt` | Release notes |
| `privacy_policy.html` | Hosts at **legalstaan.com/privacy** |
| `terms.html` | Hosts at **legalstaan.com/terms** |
| `account_deletion.html` | Hosts at **legalstaan.com/account-deletion** |
| `support.html` | Hosts at **legalstaan.com/support** |
| `metadata.txt` | Field-by-field map for Play Console + asset URLs + fingerprints |
| `data_safety_answers.md` | Cheat sheet for Data Safety form |
| `content_rating_answers.md` | Cheat sheet for IARC questionnaire |
| `README_PUBLISH.md` | This file |

The signed AAB is at:
```
app\build\outputs\bundle\release\app-release.aab     (9.74 MB, signed)
```

---

## 1. Email used everywhere — KRISHNASHELKEINTERN@GMAIL.COM

| Field | Value |
|---|---|
| Play Console developer login | `krishnashelkeintern@gmail.com` |
| Store-listing contact | `krishnashelkeintern@gmail.com` |
| Support email | `krishnashelkeintern@gmail.com` |
| Privacy contact | `krishnashelkeintern@gmail.com` |
| Account-deletion email | `krishnashelkeintern@gmail.com` |
| Content-rating questionnaire | `krishnashelkeintern@gmail.com` |

**Do NOT use** `satherutuja2398@gmail.com` anywhere in Play Console.

---

## 2. URLs used everywhere — legalstaan.com

| URL | Field in Play Console |
|---|---|
| `https://legalstaan.com` | Website |
| `https://legalstaan.com/privacy` | Privacy policy URL |
| `https://legalstaan.com/terms` | Terms (in store listing description, optional field) |
| `https://legalstaan.com/account-deletion` | Data Safety → Account-deletion URL |
| `https://legalstaan.com/support` | Support page (mention in store listing) |

---

## 3. Host the four HTML pages on legalstaan.com (one-time)

### Required URL paths
```
https://legalstaan.com/                    → index.html (home page, your choice)
https://legalstaan.com/privacy             → privacy_policy.html (rename to /privacy/index.html)
https://legalstaan.com/terms               → terms.html (rename to /terms/index.html)
https://legalstaan.com/account-deletion    → account_deletion.html (rename to /account-deletion/index.html)
https://legalstaan.com/support             → support.html (rename to /support/index.html)
```

### How to host (pick whichever applies to your setup)

#### Option A — Existing legalstaan.com hosting (cPanel / Hostinger / Bluehost / GoDaddy)
1. Log into your host's File Manager
2. Navigate to `public_html/`
3. Create folders: `privacy/`, `terms/`, `account-deletion/`, `support/`
4. Upload each HTML file into its folder, **renaming to `index.html`**:
   - `privacy_policy.html` → `public_html/privacy/index.html`
   - `terms.html` → `public_html/terms/index.html`
   - `account_deletion.html` → `public_html/account-deletion/index.html`
   - `support.html` → `public_html/support/index.html`
5. Visit each URL in browser to confirm it loads

#### Option B — GitHub Pages with custom domain (legalstaan.com)
1. Create repo `legalstaan-website` (public)
2. Folder structure:
   ```
   /index.html                          ← landing page
   /privacy/index.html                  ← rename privacy_policy.html
   /terms/index.html                    ← rename terms.html
   /account-deletion/index.html         ← rename account_deletion.html
   /support/index.html                  ← rename support.html
   /CNAME                               ← contains: legalstaan.com
   ```
3. Push to GitHub
4. Settings → Pages → Source: main / root → Custom domain: `legalstaan.com` → Enforce HTTPS
5. At your domain registrar, set DNS:
   - `A` record `@` → `185.199.108.153`, `185.199.109.153`, `185.199.110.153`, `185.199.111.153`
   - `CNAME` record `www` → `<your-github-username>.github.io`

#### Option C — Firebase Hosting (recommended if you already have Firebase project)
```powershell
npm install -g firebase-tools
firebase login --account krishnashelkeintern@gmail.com
firebase init hosting

# In the wizard:
#   public dir: public
#   single-page app: No
#   GitHub auto-deploy: optional

mkdir public\privacy, public\terms, public\account-deletion, public\support
copy play-store-listing\privacy_policy.html         public\privacy\index.html
copy play-store-listing\terms.html                  public\terms\index.html
copy play-store-listing\account_deletion.html       public\account-deletion\index.html
copy play-store-listing\support.html                public\support\index.html

# add custom domain in Firebase Console -> Hosting -> Add custom domain -> legalstaan.com
firebase deploy --only hosting
```

---

## 4. Sign in to Play Console

- URL: https://play.google.com/console
- Account: **krishnashelkeintern@gmail.com**
- If first time: pay $25 USD developer registration fee + complete identity verification (1–2 days)

---

## 5. Create the app

- Click **Create app** (top right)
- App name: `Legalstaan`
- Default language: `English (United States)`
- App or game: **App**
- Free or paid: **Free**
- Accept developer-policy + US export-laws declarations
- **Create app**

---

## 6. Set up your app — left sidebar checklist

### 6.1 App access
- All app functionality available without restrictions: **Yes** (or No + provide test login)

### 6.2 Ads
- Contains ads? **No**

### 6.3 Content rating
- Open `content_rating_answers.md`, follow line-by-line
- Email: `krishnashelkeintern@gmail.com`

### 6.4 Target audience and content
- Target age groups: **18+**
- Appeals to children: **No**

### 6.5 News app — **No**
### 6.6 COVID-19 contact tracing — **No**

### 6.7 Data safety
- Open `data_safety_answers.md`, follow line-by-line
- **Account-deletion URL**: `https://legalstaan.com/account-deletion`

### 6.8 Government apps — **No**
### 6.9 Financial features — **No**
### 6.10 Health — **No**

---

## 7. Main store listing

Sidebar → **Grow → Store presence → Main store listing**

| Field | Source |
|---|---|
| App name | `Legalstaan` |
| Short description | paste contents of `short_description.txt` |
| Full description | paste contents of `full_description.txt` |
| App icon (512×512) | upload from https://i.ibb.co/pj8RBzJ4/R05i-Av-U7.jpg |
| Feature graphic (1024×500) | upload from https://i.ibb.co/vxJ2rQ7h/pgb-H2-QM8.jpg |
| Phone screenshots (5) | upload all 5 URLs from `metadata.txt` |
| Video (optional) | skip |
| App category | **Education** |
| Tags | Education, Legal, Law |
| Email | `krishnashelkeintern@gmail.com` |
| Website | `https://legalstaan.com` |
| Phone | optional, leave blank |
| Privacy policy | `https://legalstaan.com/privacy` |

**Save**

---

## 8. Upload the AAB to Internal testing

Sidebar → **Release → Testing → Internal testing → Create new release**

1. **Use Play App Signing**: continue
2. Drag-drop `app\build\outputs\bundle\release\app-release.aab`
3. Release name: `1.30 (31)`
4. Release notes (English):
   ```
   v1.30 — UI polish, video player improvements, bug fixes
   ```
5. **Save → Review release → Start rollout to Internal testing**

### Add testers
- **Testers** tab → **Create email list** → name "Internal" → paste tester emails
- Copy the **opt-in URL**, share with testers

---

## 9. Promote through tracks

1. **Internal testing** — 1–3 days, smoke test
2. **Closed testing (alpha)** — 3–7 days
3. **Open testing (beta)** — optional public beta
4. **Production** — staged rollout

---

## 10. Production rollout

- Sidebar → **Release → Production → Create new release**
- Promote AAB from Internal/Closed
- **Staged rollout**: start at 20%, increase to 50% then 100% over 48 h
- **Review release → Start rollout to Production**

Review timeline:
- New app first review: **1–7 days**
- Subsequent updates: **hours to 1 day**

---

## 11. Future updates — re-build & re-upload

```powershell
cd "c:\Users\Pc\Downloads\Legalstaan-main prod\Legalstaan-main"

# bump version in app\build.gradle:
#   versionCode 32
#   versionName "1.31"

.\gradlew.bat clean bundleRelease

# Upload app\build\outputs\bundle\release\app-release.aab via Play Console
```

---

## Done.
