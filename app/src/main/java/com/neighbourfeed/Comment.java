package com.neighbourfeed;

public class Comment {
    private final String userName;
    private final String content;
    private final String date;

    public Comment(String userName, String commentText, String commentDate) {
        this.userName = userName;
        this.content = commentText;
        this.date = commentDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}
