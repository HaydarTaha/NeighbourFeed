package com.neighbourfeed;

import androidx.annotation.NonNull;

public class Post {
    //Post has userName, distanceFromUser, postContent, this is TextView
    //Post has postImage, this is ImageView

    private String userName;
    private String distanceFromUser;
    private String postContent;
    private int postImage;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;

    public Post(String userName, String distanceFromUser, String postContent, int likeCount, int dislikeCount, int commentCount) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
    }

    public Post(String userName, String distanceFromUser, String postContent, int postImage, int likeCount, int dislikeCount, int commentCount) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
        this.postImage = postImage;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
    }

    public String getUserName() {
        return userName;
    }

    public String getDistanceFromUser() {
        return distanceFromUser;
    }

    public String getPostContent() {
        return postContent;
    }

    public int getPostImage() {
        return postImage;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDistanceFromUser(String distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public void setPostImage(int postImage) {
        this.postImage = postImage;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @NonNull
    @Override
    public String toString() {
        return "Post{" +
                "userName='" + userName + '\'' +
                ", distanceFromUser='" + distanceFromUser + '\'' +
                ", postContent='" + postContent + '\'' +
                ", postImage=" + postImage +
                '}';
    }
}
