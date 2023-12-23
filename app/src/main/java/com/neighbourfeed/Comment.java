package com.neighbourfeed;

public class Comment {
    private final String userName;
    private final String commentText;
    private final String commentDate;

    public Comment(String userName, String commentText, String commentDate) {
        this.userName = userName;
        this.commentText = commentText;
        this.commentDate = commentDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommentDate() {
        return commentDate;
    }
}
