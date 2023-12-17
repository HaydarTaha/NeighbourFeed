package com.neighbourfeed;

public class Comment {
    private int iconResource;
    private String username;
    private String commentText;

    public Comment(int iconResource, String username, String commentText) {
        this.iconResource = iconResource;
        this.username = username;
        this.commentText = commentText;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getUsername() {
        return username;
    }

    public String getCommentText() {
        return commentText;
    }
}
