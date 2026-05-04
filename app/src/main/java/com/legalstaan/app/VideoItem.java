package com.legalstaan.app;

/**
 * Drive-backed item shown in lecture / study-material lists.
 * Same shape for static config.json entries and live Drive folder listings —
 * mimeType decides whether the item is a folder, a video, or a PDF.
 */
public class VideoItem {
    public static final String MIME_FOLDER = "application/vnd.google-apps.folder";
    public static final String MIME_PDF    = "application/pdf";

    public final String title;
    public final String fileId;
    public final String mimeType;

    /** Legacy constructor — defaults to video/mp4 (matches the static config.json entries). */
    public VideoItem(String title, String fileId) {
        this(title, fileId, "video/mp4");
    }

    public VideoItem(String title, String fileId, String mimeType) {
        this.title    = title;
        this.fileId   = fileId;
        this.mimeType = (mimeType == null || mimeType.isEmpty()) ? "video/mp4" : mimeType;
    }

    public boolean isFolder() { return MIME_FOLDER.equals(mimeType); }
    public boolean isPdf()    { return MIME_PDF.equals(mimeType); }
    public boolean isVideo()  { return mimeType != null && mimeType.startsWith("video/"); }
}
