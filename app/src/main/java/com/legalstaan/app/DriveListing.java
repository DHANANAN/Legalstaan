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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fetches a public Google Drive folder's contents via Drive API v3.
 *
 * The flat fetch ({@link #fetchFolderRecursive}) walks the folder tree
 * breadth-first up to {@link #MAX_DEPTH} levels and returns every video and
 * PDF it finds — sub-folders are not surfaced to the UI. That keeps the
 * student-facing list simple: just lectures and materials, no navigation.
 */
public class DriveListing {

    private static final String TAG      = "DriveListing";
    private static final String API_BASE = "https://www.googleapis.com/drive/v3/files";
    /** Max sub-folder depth to descend during recursive fetch. */
    private static final int    MAX_DEPTH = 4;

    public interface Callback {
        void onResult(List<VideoItem> items);
        void onError(String message);
    }

    private static final ExecutorService EXEC = Executors.newCachedThreadPool();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /**
     * Walk {@code folderId} breadth-first, return every video + PDF found in
     * the entire tree. Sub-folders themselves are not included in the result.
     * The faculty's folder structure is irrelevant to the student.
     */
    public static void fetchFolderRecursive(final String folderId, final String apiKey,
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
                List<VideoItem> files = new ArrayList<>();
                Deque<DepthEntry> queue = new ArrayDeque<>();
                queue.add(new DepthEntry(folderId, 0));

                while (!queue.isEmpty()) {
                    DepthEntry head = queue.removeFirst();
                    List<VideoItem> children = listOnce(head.id, apiKey);
                    for (VideoItem v : children) {
                        if (v.isFolder()) {
                            if (head.depth + 1 < MAX_DEPTH) {
                                queue.add(new DepthEntry(v.fileId, head.depth + 1));
                            }
                        } else if (v.isVideo() || v.isPdf()) {
                            files.add(v);
                        }
                    }
                }

                files.sort(new Comparator<VideoItem>() {
                    @Override public int compare(VideoItem a, VideoItem b) {
                        // Videos before PDFs, then alphabetical.
                        if (a.isVideo() && b.isPdf()) return -1;
                        if (a.isPdf() && b.isVideo()) return 1;
                        return a.title.compareToIgnoreCase(b.title);
                    }
                });

                MAIN.post(() -> callback.onResult(files));

            } catch (DriveError e) {
                MAIN.post(() -> callback.onError(e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "fetchFolderRecursive failed", e);
                MAIN.post(() -> callback.onError(
                        "Could not reach Drive: " + e.getMessage()));
            }
        });
    }

    /** Single-folder, one-page listing (used by the recursive walker). */
    private static List<VideoItem> listOnce(String folderId, String apiKey) throws Exception {
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
            throw new DriveError(translateError(code, err));
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
                // Skip Google-native docs (Docs, Sheets, Slides) — they don't preview cleanly.
                if (mime.startsWith("application/vnd.google-apps.")
                        && !VideoItem.MIME_FOLDER.equals(mime)) continue;
                items.add(new VideoItem(stripExtension(name), id, mime));
            }
        }
        return items;
    }

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

    /** Tracks a folder ID + depth in the BFS queue. */
    private static final class DepthEntry {
        final String id;
        final int    depth;
        DepthEntry(String id, int depth) { this.id = id; this.depth = depth; }
    }

    /** Internal exception so listOnce can short-circuit out of the BFS with a friendly message. */
    private static final class DriveError extends Exception {
        DriveError(String message) { super(message); }
    }
}
