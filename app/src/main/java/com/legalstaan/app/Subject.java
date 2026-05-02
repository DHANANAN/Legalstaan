package com.legalstaan.app;

import java.util.List;

public class Subject {
    public final String id;
    public final String title;
    public final String color;
    public final List<VideoItem> videos;
    /** "lecture" (default) or "study_material" */
    public final String category;

    public Subject(String id, String title, String color, List<VideoItem> videos, String category) {
        this.id       = id;
        this.title    = title;
        this.color    = color;
        this.videos   = videos;
        this.category = category != null ? category : "lecture";
    }

    public boolean isStudyMaterial() {
        return "study_material".equals(category);
    }

    public int getVideoCount() {
        return videos != null ? videos.size() : 0;
    }
}
