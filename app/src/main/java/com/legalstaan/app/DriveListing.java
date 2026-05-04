package com.legalstaan.app;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fetches the contents of a public Google Drive folder via Drive API v3.
 *
 * The folder must be shared as "Anyone with the link can view" and the API key
 * must have Drive API enabled. See DRIVE_SETUP.md for full instructions.
 */
public class DriveListing {

    private static final String TAG = "DriveListing";
    private static final String API_BASE = "https://www.googleapis.com/drive/v3/files";

    public interface Callback {
        void onResult(List<VideoItem> items);
        void onError(String message);
    }

    private static final ExecutorService EXEC = Executors.newCachedThreadPool();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Fetch the (non-trashed) direct children of {@code folderId}, ordered by name. */
    public static void fetchFolder(final String folderId, final String apiKey,
                                   final Callback callback) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("PASTE_")) {
            MAIN.post(() -> callback.onError(
                    "Drive API key not configured. See DRIVE_SETUP.md."));
            return;
        }
        if (folderId == null || folderId.isEmpty()) {
            MAIN.post(() -> callback.onError("No folder linked for this subject."));
            return;
        }

        EXEC.execute(() -> {
            try {
                String query = "'" + folderId + "' in parents and trashed = false";
                String url = API_BASE
                        + "?q=" + URLEncoder.encode(query, "UTF-8")
                        + "&fields=" + URLEncoder.encode("files(id,name,mimeType)", "UTF-8")
                        + "&orderBy=" + URLEncoder.encode("folder,name", "UTF-8")
                        + "&pageSize=200"
                        + "&supportsAllDrives=true"
                        + "&includeItemsFromAllDrives=true"
                        + "&key=" + URLEncoder.encode(apiKey, "UTF-8");

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);

                int code = conn.getResponseCode();
                if (code != 200) {
                    String err = readStream(conn.getErrorStream());
                    Log.e(TAG, "HTTP " + code + ": " + err);
                    MAIN.post(() -> callback.onError(translateError(code, err)));
                    return;
                }

                String body = readStream(conn.getInputStream());
                conn.disconnect();

                JSONObject root  = new JSONObject(body);
                JSONArray  files = root.optJSONArray("files");
                List<VideoItem> items = new ArrayList<>();
                if (files != null) {
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject f = files.getJSONObject(i);
                        String name = f.optString("name", "(untitled)");
                        String id   = f.optString("id");
                        String mime = f.optString("mimeType", "");
                        // Skip Google-native docs (Docs, Sheets, Slides) — they
                        // don't preview cleanly in our WebView.
                        if (mime.startsWith("application/vnd.google-apps.")
                                && !VideoItem.MIME_FOLDER.equals(mime)) continue;
                        items.add(new VideoItem(stripExtension(name), id, mime));
                    }
                }
                // Folders first, then videos, then PDFs, then anything else.
                items.sort(new Comparator<VideoItem>() {
                    @Override public int compare(VideoItem a, VideoItem b) {
                        int ra = rank(a);
                        int rb = rank(b);
                        if (ra != rb) return Integer.compare(ra, rb);
                        return a.title.compareToIgnoreCase(b.title);
                    }
                });
                MAIN.post(() -> callback.onResult(items));

            } catch (Exception e) {
                Log.e(TAG, "fetchFolder failed", e);
                MAIN.post(() -> callback.onError(
                        "Could not reach Drive: " + e.getMessage()));
            }
        });
    }

    private static int rank(VideoItem v) {
        if (v.isFolder()) return 0;
        if (v.isVideo())  return 1;
        if (v.isPdf())    return 2;
        return 3;
    }

    /** Trim trailing ".mp4" / ".pdf" etc. so titles render cleanly. */
    private static String stripExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot > name.length() - 6) return name.substring(0, dot);
        return name;
    }

    private static String readStream(java.io.InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    /** Translate Drive API errors into messages a non-developer can act on. */
    private static String translateError(int code, String body) {
        String low = body == null ? "" : body.toLowerCase();
        if (code == 403 && low.contains("daily limit"))
            return "Drive API daily quota exhausted. Try again tomorrow or upgrade quota.";
        if (code == 403)
            return "Access denied. Make sure the folder is shared 'Anyone with the link' and the API key allows your app.";
        if (code == 404)
            return "Folder not found. Check the folder ID in config.json.";
        if (code == 400 && low.contains("api key"))
            return "Drive API key is invalid. See DRIVE_SETUP.md.";
        if (code == 429)
            return "Too many Drive requests right now — please try again in a minute.";
        return "Drive returned HTTP " + code + ".";
    }
}
