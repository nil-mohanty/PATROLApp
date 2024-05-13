package com.example.patrol;

public class BroadcastItem {
    private int message_id;
    private String timestamp;
    private String title;
    private String message;
    private boolean expanded;

    public BroadcastItem(int message_id, String date, String shortText, String fullText) {
        this.message_id = message_id;
        this.timestamp = date;
        this.title = shortText;
        this.message = fullText;
        this.expanded = false;
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean toggleExpanded() {
        this.expanded = !this.expanded;
        return this.expanded;
    }

    @Override
    public String toString() {
        return "BroadcastItem{" +
                "message_id=" + message_id +
                ", timestamp='" + timestamp + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
