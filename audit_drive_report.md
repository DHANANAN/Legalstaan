# Google Drive Asset Audit Report - Legalstaan v1.35

**Audit Date:** August 6, 2026  
**Target Drive Root Folder:** `1C6MUUCFbKD5MglfikKP6ancKjJTxYJ1Z` (`https://drive.google.com/drive/folders/1C6MUUCFbKD5MglfikKP6ancKjJTxYJ1Z?usp=drive_link`)  
**Target App Package:** `com.legalstaan.app`

---

## 1. Audit Summary

| Category | Count | Status / Action |
|---|---|---|
| **Total Links Checked** | 106 | Complete baseline asset inventory |
| **Accessible Assets (HTTP 200 OK)** | 66 | Retained in `config.json` |
| **Restricted Assets (HTTP 401/403)** | 30 | Handled via in-app static fallback logic in `SubjectVideosActivity.java` |
| **Deleted / Not Found (HTTP 404)** | 13 | Pruned from `config.json` |
| **Reconciled Valid Items** | 51 | Aligned with Drive folder contents and user screenshots |

---

## 2. Screenshot & Folder Structure Reconciliation

User provided 4 authoritative Google Drive folder screenshots:

1. **Trademark Law (`trademark_law`)**:
   - Screenshot files present: `Trademark 1.mp4`, `Trademark lecture 4.MP4`, `Trademark 5.mp4`, `Trademark 6.mp4`, `Trademark 7.MP4`, `Trademarks 8.mp4`.
   - Action: Retained Lectures 1, 4, 5, 6, 7, 8 in `config.json`. Removed non-existent lecture placeholders.

2. **Admin Law (`admin_law`)**:
   - Screenshot files present: `Admin Law 1.mp4`, `Admin Law 2.mp4`, `Admin Law 3.mp4`.
   - Action: Removed non-existent `Admin Law - Lecture 4`.

3. **Design Act 2000 (`design_act`)**:
   - Screenshot files present: `Design Act 1.mp4`, `Design Act 2.mp4`, `Design Act 3.mp4`, `Design Act 4.mp4`, `Design Act 5.mp4`, `Design Act 6.mp4`.
   - Action: Removed non-existent Lectures 7 & 8.

4. **Environmental Law (`enviro_law`)**:
   - Screenshot files present: `Enviro1.mp4`, `Enviro 2.mp4`.

---

## 3. Removed 404 Deleted Links List

The following 13 items returned HTTP 404 or were verified missing from Drive folders and have been removed from `config.json`:

1. `trademark_law` — Trademark Law - Lecture 10 (`1cNpDbTRYbxRykqtjj0p584NIhSjytDG3`)
2. `trademark_law` — Trademark Law - Lecture 16 (`1nitAj6IzltWfQqGMAorm-k9upsIwqgs_`)
3. `plant_variety` — Plant Variety & Farmers Rights Act - Lecture 1 (`14iCz9EIugOUERJsPTRnkyeRpaXw6Z7Tt`)
4. `copyright_law` — Copyright Law - Lecture 5 (`1YJXudb_c2Ix3CpAgE58TujbR_8-2xVhn`)
5. `copyright_law` — Copyright Law - Lecture 6 (`1POULhFf6KSKfVsR6zbZa0ggcMgA4IC45`)
6. `copyright_law` — Copyright Law - Lecture 7 (`12fOBWJKY-2NiNcY1cMVsovZNivfpy9pg`)
7. `copyright_law` — Copyright Law - Lecture 8 (`1Fm2PtzjgvKJXGqRso6WqjueV8QwYk5Aq`)
8. `copyright_law` — Copyright Law - Lecture 9 (`1QDyGKDeZW3RU5M_6K49xA2j_k1A3lKLY`)
9. `copyright_law` — Copyright Law - Lecture 10 (`1DWP8ZACeZX7h4rEw23hpEXr-12FTIo0E`)
10. `constitution` — Constitution - Lecture 1 (`1g9_Act4s_77m8Xo9gdlQmhrSXP853p4u`)
11. `admin_law` — Administrative Law - Lecture 4 (`1dB1dXxsuYeTOXL8TDv-preS1r32hAIHw`)
12. `design_act` — Design Act 2000 - Lecture 7 (`1CR7VRYumg7PE6vyute0J94JJ59XCPHKI`)
13. `design_act` — Design Act 2000 - Lecture 8 (`1Uv9QLvw7tMCI6dvIab3OzIXBhz0fN_x7`)

---

## 4. Fallback Architecture

In `SubjectVideosActivity.java`:
- When a user opens a subject with `folder_id` `1C6MUUCFbKD5MglfikKP6ancKjJTxYJ1Z`, live Drive API listing is executed.
- If live fetch fails or returns restricted/empty results (e.g. offline network or quota), `fallbackToStaticConfig()` loads valid static videos for that subject from `config.json`.
