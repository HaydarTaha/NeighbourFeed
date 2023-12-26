package com.neighbourfeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import java.util.List;

public class UserProfile extends AppCompatActivity {

    TextView textViewUserName;
    String userName;
    String email;
    String uid;
    FirebaseFirestore db;
    boolean isStart = true;
    private double latitude;
    private double longitude;
    private ArrayList<Post> userPosts;
    private ArrayList<Comment> userComments;
    private ArrayList<Post> userUpVotedPosts;
    private ArrayList<Post> userDownVotedPosts;
    PostAdapter adapter;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        textViewUserName = findViewById(R.id.textViewUsername);
        textViewUserName.setText(intent.getStringExtra("userName"));
        userName = intent.getStringExtra("userName");
        email = intent.getStringExtra("email");
        uid = intent.getStringExtra("uid");
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);

        // Get checkedId from intent
        int checkedId = intent.getIntExtra("checkedId", R.id.btnPosts);

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);

        toggleGroup.check(R.id.btnPosts);

        db = FirebaseFirestore.getInstance();

        userPosts = new ArrayList<>();
        userComments = new ArrayList<>();
        userUpVotedPosts = new ArrayList<>();
        userDownVotedPosts = new ArrayList<>();

        getPosts();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        TextView headerTextView = findViewById(R.id.headerTextView);
        TextView textViewHeaderUserProfile = findViewById(R.id.textViewHeaderUserProfile);
        ImageButton profileButton = findViewById(R.id.profileButton);
        ProgressBar progressBar = findViewById(R.id.progressBarUserProfile);
        ListView postListView = findViewById(R.id.postListView);
        profileButton.setClickable(false);

        progressBar.setVisibility(View.VISIBLE);

        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            progressBar.setVisibility(View.VISIBLE);
            if (isChecked) {
                progressBar.setVisibility(View.INVISIBLE);
                textViewHeaderUserProfile.setVisibility(View.VISIBLE);
                if (checkedId == R.id.btnPosts) {
                    textViewHeaderUserProfile.setText("Posts");
                    if (isStart) {
                        isStart = false;
                    } else {
                        userPosts = new ArrayList<>();
                        getPosts();
                    }
                } else if (checkedId == R.id.btnComments) {
                    textViewHeaderUserProfile.setText("Comments");
                    userComments = new ArrayList<>();
                    getComments();
                } else if (checkedId == R.id.btnUpVotes) {
                    textViewHeaderUserProfile.setText("Up Voted Posts");
                    userUpVotedPosts = new ArrayList<>();
                    getUpVotedPosts();
                } else if (checkedId == R.id.btnDownVotes) {
                    textViewHeaderUserProfile.setText("Down Voted Posts");
                    userDownVotedPosts = new ArrayList<>();
                    getDownVotedPosts();
                }
            }
        });
    }

    private void getPosts() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        db.collection("Posts")
                .whereEqualTo("userName", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Process fetched posts and apply filters
                        for (int i = 0; i < task.getResult().size(); i++) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(i);
                            Post post = processPostData(document);
                            fetchCommentsWithID(document.getId(), post);

                            userPosts.add(post);
                        }

                        // Update the UI after fetching and filtering posts
                        if (!userPosts.isEmpty()) {
                            ProgressBar progressBar = findViewById(R.id.progressBarUserProfile);
                            progressBar.setVisibility(View.GONE);
                            PostAdapter adapter = new PostAdapter(this, userPosts, userName);
                            @SuppressLint("CutPasteId") ListView listView = findViewById(R.id.postListView);
                            listView.setAdapter(adapter);
                        }else {
                            Log.d("Post", "Post list is empty");
                        }
                    } else {
                        Log.d("UserProfile", "Error getting documents: ", task.getException());
                    }
                });
        adapter = new PostAdapter(this, userPosts, userName);
        @SuppressLint("CutPasteId") ListView postListView = findViewById(R.id.postListView);
        postListView.setAdapter(adapter);
    }

    private void getComments() {
        db.collection("Comments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot commentDoc : Objects.requireNonNull(task.getResult())) {
                            ArrayList<Object> commentList = (ArrayList<Object>) commentDoc.get("comments");
                            if (commentList != null) {
                                for (Object commentObject : commentList) {
                                    if (commentObject instanceof HashMap) {
                                        Map<String, Object> commentMap = (Map<String, Object>) commentObject;
                                        String commentUserName = (String) commentMap.get("userName");
                                        if (commentUserName != null && commentUserName.equals(userName)) {
                                            String commentContent = (String) commentMap.get("content");
                                            Timestamp date = (Timestamp) commentMap.get("date");
                                            assert date != null;
                                            Date commentDate = date.toDate();
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                            String dateString = sdf.format(commentDate);
                                            Comment comment = new Comment(commentUserName, commentContent, dateString, commentDoc.getId());
                                            userComments.add(comment);

                                        }
                                    }
                                }
                            }
                        }

                        if (!userComments.isEmpty()) {
                            ProgressBar progressBar = findViewById(R.id.progressBarUserProfile);
                            progressBar.setVisibility(View.GONE);
                            CommentAdapter adapter = new CommentAdapter(this, userComments, true, userName, latitude, longitude);
                            @SuppressLint("CutPasteId") ListView listView = findViewById(R.id.postListView);
                            listView.setAdapter(adapter);
                        } else {
                            Log.d("Comment", "Comment list is empty");
                        }
                    }
                });
    }

    private void getUpVotedPosts() {
        db.collection("Posts")
                .whereArrayContains("upVotedUsers", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Process fetched posts and apply filters
                        for (int i = 0; i < task.getResult().size(); i++) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(i);
                            Post post = processPostData(document);
                            fetchCommentsWithID(document.getId(), post);

                            userUpVotedPosts.add(post);
                        }

                        // Update the UI after fetching and filtering posts
                        if (!userUpVotedPosts.isEmpty()) {
                            ProgressBar progressBar = findViewById(R.id.progressBarUserProfile);
                            progressBar.setVisibility(View.GONE);
                            PostAdapter adapter = new PostAdapter(this, userUpVotedPosts, userName);
                            @SuppressLint("CutPasteId") ListView listView = findViewById(R.id.postListView);
                            listView.setAdapter(adapter);
                        }else {
                            Log.d("Post", "Post list is empty");
                        }
                    } else {
                        Log.d("UserProfile", "Error getting documents: ", task.getException());
                    }
                });
                adapter = new PostAdapter(this, userUpVotedPosts, userName);
                @SuppressLint("CutPasteId") ListView postListView = findViewById(R.id.postListView);
                postListView.setAdapter(adapter);
    }

    private void getDownVotedPosts() {
        db.collection("Posts")
                .whereArrayContains("downVotedUsers", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Process fetched posts and apply filters
                        for (int i = 0; i < task.getResult().size(); i++) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(i);
                            Post post = processPostData(document);
                            fetchCommentsWithID(document.getId(), post);

                            userDownVotedPosts.add(post);
                        }

                        // Update the UI after fetching and filtering posts
                        if (!userDownVotedPosts.isEmpty()) {
                            ProgressBar progressBar = findViewById(R.id.progressBarUserProfile);
                            progressBar.setVisibility(View.GONE);
                            PostAdapter adapter = new PostAdapter(this, userDownVotedPosts, userName);
                            @SuppressLint("CutPasteId") ListView listView = findViewById(R.id.postListView);
                            listView.setAdapter(adapter);
                        }else {
                            Log.d("Post", "Post list is empty");
                        }
                    } else {
                        Log.d("UserProfile", "Error getting documents: ", task.getException());
                    }
                });
        adapter = new PostAdapter(this, userDownVotedPosts, userName);
        @SuppressLint("CutPasteId") ListView postListView = findViewById(R.id.postListView);
        postListView.setAdapter(adapter);
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
        Log.d("MainActivity", "Post: " + content + " " + createDate + " " + downVotedUsers + " " + mediaPath + " " + mediaType + " " + location + " " + type + " " + upVotedUsers + " " + userName + " " + id);

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

    private void fetchCommentsWithID(String id, Post post) {
        Log.d("Comment", "Post id: " + id);
        //Fetch posts from firebase database
        db.collection("Comments")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            ArrayList<Object> commentList = (ArrayList<Object>) documentSnapshot.get("comments");
                            if (commentList != null) {
                                post.setCommentCount(commentList.size());
                                adapter.notifyDataSetChanged();
                                Log.d("Comment", "Comment count: " + commentList.size());
                            }
                        }
                    } else {
                        Log.d("Comment", "Error getting document: ", task.getException());
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userName", userName);
        outState.putString("email", email);
        outState.putString("uid", uid);
        outState.putDouble("latitude", latitude);
        outState.putDouble("longitude", longitude);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        userName = savedInstanceState.getString("userName");
        email = savedInstanceState.getString("email");
        uid = savedInstanceState.getString("uid");
        latitude = savedInstanceState.getDouble("latitude");
        longitude = savedInstanceState.getDouble("longitude");
    }
}
