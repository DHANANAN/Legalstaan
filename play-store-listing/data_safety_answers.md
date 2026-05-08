# Data Safety Form — Answer Sheet

Play Console → Policy → App content → Data safety → Start

## 1. Data collection and security

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | **Yes** |
| Do you provide a way for users to request that their data be deleted? | **Yes** — both via web form at https://legalstaan.com/account-deletion AND via email to krishnashelkeintern@gmail.com |
| Account deletion URL (required) | **https://legalstaan.com/account-deletion** |

## 2. Data Types — declare each item below

### Personal info → Email address
- Collected: **Yes**
- Shared: **No**
- Required or optional: **Required** (login)
- Purpose: **Account management**, **App functionality**
- Source: User provided (Google Sign-In)

### Personal info → Name
- Collected: **Yes** (from Google profile)
- Shared: **No**
- Required or optional: **Required**
- Purpose: **Account management**, **App functionality**

### Personal info → User IDs
- Collected: **Yes** (Firebase Auth UID, FCM token)
- Shared: **No**
- Required or optional: **Required**
- Purpose: **Account management**, **App functionality**, **Analytics**

### Photos and videos → Photos
- Collected: **Yes** (only if user uploads profile picture)
- Shared: **No**
- Required or optional: **Optional**
- Purpose: **App functionality** (profile customization)

### App activity → App interactions
- Collected: **Yes**
- Shared: **No**
- Required or optional: **Optional**
- Purpose: **Analytics**, **App functionality**

### App info and performance → Crash logs
- Collected: **Yes**
- Shared: **No**
- Required or optional: **Optional**
- Purpose: **Analytics** (improve stability)

### App info and performance → Diagnostics
- Collected: **Yes**
- Shared: **No**
- Required or optional: **Optional**
- Purpose: **Analytics**

### Device or other IDs
- Collected: **Yes** (FCM token, Firebase installation ID)
- Shared: **No**
- Required or optional: **Required**
- Purpose: **App functionality** (push notifications)

## 3. Data NOT collected (declare "No")

- Location (precise / approximate) — **No**
- Financial info — **No**
- Health and fitness — **No**
- Messages (SMS/MMS/email content) — **No**
- Audio files — **No**
- Files and docs — **No**
- Calendar — **No**
- Contacts — **No**
- Web browsing — **No**
- Installed apps — **No**

## 4. Data shared with third parties

**Answer: No** — Firebase is a data processor for us, not a third-party share recipient. Google Gemini API receives only the AI-assistant prompts the user types (declare under "Messages" only if you classify chat with the AI assistant as "Messages"; standard practice is to not classify AI assistant prompts as user-to-user messages).

## 5. Security practices

- ✅ Data is encrypted in transit
- ✅ You can request that data be deleted
- ✅ Committed to follow Play Families Policy: **No** (app targets 16+, not children)

---

**Save → Next → Review → Submit**
