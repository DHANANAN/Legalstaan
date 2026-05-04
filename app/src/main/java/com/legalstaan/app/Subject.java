package com.legalstaan.app;

import java.util.List;

public class Subject {
    public final String id;
    public final String title;
    public final String color;
    public final List<VideoItem> videos;
    /** "lecture" (default) or "study_material" */
    public final String category;
    /** Optional Drive folder ID — when set, the subject loads its contents live. */
    public final String folderId;

    public Subject(String id, String title, String color, List<VideoItem> videos,
                   String category, String folderId) {
        this.id       = id;
        this.title    = title;
        this.color    = color;
        this.videos   = videos;
        this.category = category != null ? category : "lecture";
        this.folderId = folderId;
    }

    public boolean isStudyMaterial() {
        return "study_material".equals(category);
    }

    public int getVideoCount() {
        return videos != null ? videos.size() : 0;
    }

    public boolean isDriveLinked() {
        return folderId != null && !folderId.isEmpty();
    }
}
