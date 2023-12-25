package com.neighbourfeed;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Post implements Parcelable {
    private String userName;
    private String distanceFromUser;
    private String postContent;
    private String mediaType;
    private String mediaPath;
    private int upVoteCount;
    private int downVoteCount;
    private int commentCount;
    private String type;
    private boolean isUpVotedByUser;
    private boolean isDownVotedByUser;
    private String postId;

    public Post(String userName, String distanceFromUser, String postContent, int upVoteCount, int downVoteCount, int commentCount, String type, boolean isUpVotedByUser, boolean isDownVotedByUser, String postId, String mediaType, String mediaPath) {
        this.userName = userName;
        this.distanceFromUser = distanceFromUser;
        this.postContent = postContent;
        this.upVoteCount = upVoteCount;
        this.downVoteCount = downVoteCount;
        this.commentCount = commentCount;
        this.type = type;
        this.isUpVotedByUser = isUpVotedByUser;
        this.isDownVotedByUser = isDownVotedByUser;
        this.postId = postId;
        this.mediaType = mediaType;
        this.mediaPath = mediaPath;
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

    public String getMediaType() {
        return mediaType;
    }

    public String getMediaPath() {
        return mediaPath;
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

    public String getPostId() {
        return postId;
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

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
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

    public void setPostId(String postId) {
        this.postId = postId;
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
                ", mediaType='" + mediaType + '\'' +
                ", mediaPath='" + mediaPath + '\'' +
                ", upVoteCount=" + upVoteCount +
                ", downVoteCount=" + downVoteCount +
                ", commentCount=" + commentCount +
                ", type='" + type + '\'' +
                ", isUpVotedByUser=" + isUpVotedByUser +
                ", isDownVotedByUser=" + isDownVotedByUser +
                ", postId='" + postId + '\'' +
                '}';
    }

    // Parcelable Creator
    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    // Parcelable read
    protected Post(Parcel in) {
        userName = in.readString();
        distanceFromUser = in.readString();
        postContent = in.readString();
        mediaType = in.readString();
        mediaPath = in.readString();
        upVoteCount = in.readInt();
        downVoteCount = in.readInt();
        commentCount = in.readInt();
        type = in.readString();
        isUpVotedByUser = in.readByte() != 0;
        isDownVotedByUser = in.readByte() != 0;
        postId = in.readString();
    }

    // Parcelable write
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(distanceFromUser);
        dest.writeString(postContent);
        dest.writeString(mediaType);
        dest.writeString(mediaPath);
        dest.writeInt(upVoteCount);
        dest.writeInt(downVoteCount);
        dest.writeInt(commentCount);
        dest.writeString(type);
        dest.writeByte((byte) (isUpVotedByUser ? 1 : 0));
        dest.writeByte((byte) (isDownVotedByUser ? 1 : 0));
        dest.writeString(postId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}