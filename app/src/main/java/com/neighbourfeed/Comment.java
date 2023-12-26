package com.neighbourfeed;

public class Comment {
    private final String userName;
    private final String content;
    private final String date;
    private final String postId;

    public Comment(String userName, String commentText, String commentDate, String postId) {
        this.userName = userName;
        this.content = commentText;
        this.date = commentDate;
        this.postId = postId;
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

    public String getPostId() {
        return postId;
    }
}
