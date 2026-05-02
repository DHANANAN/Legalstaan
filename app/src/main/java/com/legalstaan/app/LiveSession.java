package com.legalstaan.app;

import com.google.firebase.Timestamp;

public class LiveSession {
    private String sessionId;
    private String facultyEmail;
    private String facultyName;
    private String title;
    private String platform; // "jitsi", "youtube", or "meet"
    private String roomId;
    private String youtubeUrl;
    private String meetUrl;
    private boolean live;
    private Timestamp startedAt;

    public LiveSession() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String v) { this.sessionId = v; }
    public String getFacultyEmail() { return facultyEmail; }
    public void setFacultyEmail(String v) { this.facultyEmail = v; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String v) { this.facultyName = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getPlatform() { return platform; }
    public void setPlatform(String v) { this.platform = v; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String v) { this.roomId = v; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String v) { this.youtubeUrl = v; }
    public String getMeetUrl() { return meetUrl; }
    public void setMeetUrl(String v) { this.meetUrl = v; }
    public boolean isLive() { return live; }
    public void setLive(boolean v) { this.live = v; }
    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp v) { this.startedAt = v; }
}
