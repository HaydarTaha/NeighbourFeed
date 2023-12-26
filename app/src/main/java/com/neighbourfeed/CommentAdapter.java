package com.neighbourfeed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private boolean isUserProfile;
    private String userName;
    private double latitude;
    private double longitude;
    public CommentAdapter(Context context, ArrayList<Comment> comments, boolean isUserProfile, String userName, double latitude, double longitude) {
        super(context, 0, comments);
        this.isUserProfile = isUserProfile;
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_comment, parent, false);
        }

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickComment(getItem(position));
            }
        });

        Comment currentComment = getItem(position);

        TextView commentUsername = listItemView.findViewById(R.id.commentUsername);
        TextView commentText = listItemView.findViewById(R.id.commentText);

        assert currentComment != null;
        commentUsername.setText(currentComment.getUserName());
        commentText.setText(currentComment.getContent());

        return listItemView;
    }

    private void onClickComment(Comment currentComment) {
        if (isUserProfile) {
            FirebaseFirestore db;
            db = FirebaseFirestore.getInstance();
            db.collection("Posts").document(currentComment.getPostId()).get().addOnSuccessListener(documentSnapshot -> {
                Log.d("CommentAdapter", "onClickComment: " + documentSnapshot.getData());
                Intent intent = new Intent(getContext(), PostPage.class);
                Post post = processPostData(documentSnapshot);
                intent.putExtra("postId", currentComment.getPostId());
                intent.putExtra("post", post);
                intent.putExtra("userName", userName);
                intent.putExtra("isLiked", post.isUpVotedByUser());
                intent.putExtra("isDisliked", post.isDownVotedByUser());
                intent.putExtra("totalVotes", post.getUpVoteCount() - post.getDownVoteCount());
                intent.putExtra("createDate", post.getPostDate());
                intent.putExtra("latitude", post.getLatitude());
                intent.putExtra("longitude", post.getLongitude());
                getContext().startActivity(intent);
            });
        }

    }

    private Post processPostData(DocumentSnapshot document) {
        String content = document.getString("content");
        Timestamp createDateTimestamp = document.getTimestamp("createDate");

        // Convert timestamp to string with format: "dd/MM/yyyy"
        assert createDateTimestamp != null;
        @SuppressLint("SimpleDateFormat") String createDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(createDateTimestamp.getSeconds() * 1000));
        List<String> downVotedUsers = castObjectToList(document.get("downVotedUsers"));
        String mediaPath = document.getString("mediaPath");
        String mediaType = document.getString("mediaType");
        String location = Objects.requireNonNull(document.getGeoPoint("location")).toString();
        String type = document.getString("type");
        List<String> upVotedUsers = castObjectToList(document.get("upVotedUsers"));
        String userName = document.getString("userName");
        String id = document.getId();

        int upVotes = upVotedUsers.size();
        int downVotes = downVotedUsers.size();

        //Calculate distance from user
        GeoPoint locationGeoPoint = document.getGeoPoint("location");
        assert locationGeoPoint != null;
        Log.d("MainActivity", "Location: " + locationGeoPoint.getLatitude() + " " + locationGeoPoint.getLongitude());
        double distance = calculateDistanceFromUser(locationGeoPoint);
        distance = Math.round(distance * 10.0) / 10.0;
        String distanceString = Double.toString(distance) + " km";

        //Check if user has upVoted or downVoted
        boolean upVoted = false;
        for (String upVotedUser : upVotedUsers) {
            if (upVotedUser.equals(this.userName)) {
                upVoted = true;
                break;
            }
        }
        boolean downVoted = false;
        for (String downVotedUser : downVotedUsers) {
            if (downVotedUser.equals(this.userName)) {
                downVoted = true;
                break;
            }
        }

        return new Post(userName, distanceString, content, upVotes, downVotes, 0, type, upVoted, downVoted, id, mediaType, mediaPath, createDate, locationGeoPoint.getLatitude(), locationGeoPoint.getLongitude());
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castObjectToList(Object object) {
        if (object instanceof List<?>) {
            return (List<T>) object;
        } else {
            // Handle the case where the object is not a List (or is null)
            return new ArrayList<>();
        }
    }

    private double calculateDistanceFromUser(GeoPoint location) {
        double dLat = Math.toRadians(location.getLatitude() - latitude);
        double dLon = Math.toRadians(location.getLongitude() - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(location.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        final int R = 6371; // Radius of the Earth in kilometers
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
