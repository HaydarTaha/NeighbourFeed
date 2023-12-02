package com.neighbourfeed;

import androidx.annotation.NonNull;

public class Post {
    //Post has userName, distanceFromUser, postContent, this is TextView
    //Post has postImage, this is ImageView

    private String userName;
    private String distanceFromUser;
    private String postContent;
    private int postImage;
    private int upVoteCount;
    private int downVoteCount;
    private int commentCount;
    private String type;
    private boolean isUpVotedByUser;
    private boolean isDownVotedByUser;

    public Post(String userName, String distanceFromUser, String postContent, int upVoteCount, int downVoteCount, int commentCount, String type, boolean isUpVotedByUser, boolean isDownVotedByUser) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
        this.upVoteCount = upVoteCount;
        this.downVoteCount = downVoteCount;
        this.commentCount = commentCount;
        this.type = type;
        this.isUpVotedByUser = isUpVotedByUser;
        this.isDownVotedByUser = isDownVotedByUser;
    }

    public Post(String userName, String distanceFromUser, String postContent, int postImage, int upVoteCount, int downVoteCount, int commentCount, String type, boolean isUpVotedByUser, boolean isDownVotedByUser) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
        this.postImage = postImage;
        this.upVoteCount = upVoteCount;
        this.downVoteCount = downVoteCount;
        this.commentCount = commentCount;
        this.type = type;
        this.isUpVotedByUser = isUpVotedByUser;
        this.isDownVotedByUser = isDownVotedByUser;
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

    public int getUpVoteCount() {
        return upVoteCount;
    }

    public int getDownVoteCount() {
        return downVoteCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public String getType() {
        return type;
    }

    public boolean isUpVotedByUser() {
        return isUpVotedByUser;
    }

    public boolean isDownVotedByUser() {
        return isDownVotedByUser;
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

    public void setUpVoteCount(int upVoteCount) {
        this.upVoteCount = upVoteCount;
    }

    public void setDownVoteCount(int downVoteCount) {
        this.downVoteCount = downVoteCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUpVotedByUser(boolean upVotedByUser) {
        isUpVotedByUser = upVotedByUser;
    }

    public void setDownVotedByUser(boolean downVotedByUser) {
        isDownVotedByUser = downVotedByUser;
    }

    public void incrementUpVoteCount() {
        this.upVoteCount++;
    }

    public void decrementUpVoteCount() {
        this.upVoteCount--;
    }

    public void incrementDownVoteCount() {
        this.downVoteCount++;
    }

    public void decrementDownVoteCount() {
        this.downVoteCount--;
    }

    @NonNull
    @Override
    public String toString() {
        return "Post{" +
                "userName='" + userName + '\'' +
                ", distanceFromUser='" + distanceFromUser + '\'' +
                ", postContent='" + postContent + '\'' +
                ", postImage=" + postImage +
                ", upVoteCount=" + upVoteCount +
                ", downVoteCount=" + downVoteCount +
                ", commentCount=" + commentCount +
                ", type='" + type + '\'' +
                '}';
    }
}
