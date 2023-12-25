package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class PostPage extends AppCompatActivity {

    String postId;
    FirebaseFirestore database;
    FirebaseStorage storage;
    Post post;
    String imageUri;
    String audioUri;
    User user;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        post = intent.getParcelableExtra("post");
        user = (User) intent.getSerializableExtra("user");
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fetchCommentsWithID(postId, post);
        putPostData(post);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        ImageButton commentButton = findViewById(R.id.iconComment);
        commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(PostPage.this, CommentPage.class);
            intent.putExtra("postId", postId);
            intent.putExtra("userName", user.getUserName());
            startActivity(intent);
        });

        ImageButton upVoteButton = findViewById(R.id.upVoteIcon);
        upVoteButton.setOnClickListener(v -> {
            //TODO: Implement upvote
        });

        ImageButton downVoteButton = findViewById(R.id.downVoteIcon);
        downVoteButton.setOnClickListener(v -> {
            //TODO: Implement downvote
        });
    }

    private void fetchCommentsWithID(String id, Post post) {
        Log.d("Comment", "Post id: " + id);
        //Fetch posts from firebase database
        database.collection("Comments")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            ArrayList<Object> commentList = (ArrayList<Object>) documentSnapshot.get("comments");
                            if (commentList != null) {
                                TextView commentCount = findViewById(R.id.textCommentCount);
                                commentCount.setText(String.valueOf(commentList.size()));
                                post.setCommentCount(commentList.size());
                                Log.d("Comment", "Comment count: " + commentList.size());
                            }
                        }
                    } else {
                        Log.d("Comment", "Error getting document: ", task.getException());
                    }
                });
    }

    private void putPostData(Post post) {
        //Set post username
        TextView userName = findViewById(R.id.textUsername);
        userName.setText(post.getUserName());

        //Set post distance
        TextView distance = findViewById(R.id.textDistance);
        distance.setText(post.getDistanceFromUser());

        //Set post content
        TextView postContent = findViewById(R.id.textPost);
        postContent.setText(post.getPostContent());

        //Set post Media
        if (post.getMediaType().equals("image")) {
            imageUri = post.getMediaPath();
            StorageReference imageReference = storage.getReferenceFromUrl(imageUri);
            ImageView imagePost = findViewById(R.id.imagePost);
            MaterialCardView imageCard = findViewById(R.id.imagePostCard);
            imageReference.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imagePost.setImageBitmap(bitmap);
                imageCard.setVisibility(MaterialCardView.VISIBLE);
                imagePost.setVisibility(ImageView.VISIBLE);
            }).addOnFailureListener(e -> Log.d("Post", "Error getting image: " + e.getMessage()));
        } else if (post.getMediaType().equals("audio")) {
            audioUri = post.getMediaPath();
            StorageReference audioReference = storage.getReferenceFromUrl(audioUri);
            View audioPost = findViewById(R.id.audioPlayerLayout);
            MaterialCardView audioCard = findViewById(R.id.audioPostCard);
            audioReference.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                audioCard.setVisibility(MaterialCardView.VISIBLE);
                audioPost.setVisibility(View.VISIBLE);
            }).addOnFailureListener(e -> Log.d("Post", "Error getting audio: " + e.getMessage()));
            Log.d("Post", "Audio uri: " + audioUri);
        }
    }
}