package com.legalstaan.app;

import java.util.List;

public class Subject {
    public final String id;
    public final String title;
    public final String color;
    public final List<VideoItem> videos;

    public Subject(String id, String title, String color, List<VideoItem> videos) {
        this.id     = id;
        this.title  = title;
        this.color  = color;
        this.videos = videos;
    }

    public int getVideoCount() {
        return videos != null ? videos.size() : 0;
    }
}
