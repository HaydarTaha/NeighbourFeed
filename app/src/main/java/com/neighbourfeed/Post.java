package com.neighbourfeed;

import androidx.annotation.NonNull;

public class Post {
    //Post has userName, distanceFromUser, postContent, this is TextView
    //Post has postImage, this is ImageView

    private String userName;
    private String distanceFromUser;
    private String postContent;
    private int postImage;

    public Post(String userName, String distanceFromUser, String postContent) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
    }

    public Post(String userName, String distanceFromUser, String postContent, int postImage) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
        this.postImage = postImage;
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
