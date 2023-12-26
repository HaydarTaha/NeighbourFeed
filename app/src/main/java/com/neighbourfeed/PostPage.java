package com.neighbourfeed;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PostPage extends AppCompatActivity {

    String postId;
    FirebaseFirestore database;
    FirebaseStorage storage;
    Post post;
    String imageUri;
    String audioUri;
    String userName;
    boolean playPause = false;
    MediaPlayer mediaPlayer;
    Thread musicThread;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        post = intent.getParcelableExtra("post");
        userName = intent.getStringExtra("userName");
        boolean isLiked = intent.getBooleanExtra("isLiked", false);
        boolean isDisliked = intent.getBooleanExtra("isDisliked", false);
        int totalVotes = intent.getIntExtra("totalVotes", 0);
        String createdDate = intent.getStringExtra("createDate");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fetchCommentsWithID(postId, post);
        putPostData(post);

        // Set up vote and down vote icons
        ImageButton upVoteButton = findViewById(R.id.upVoteIcon);
        ImageButton downVoteButton = findViewById(R.id.downVoteIcon);
        upVoteButton.setImageResource(isLiked ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
        downVoteButton.setImageResource(isDisliked ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);

        // Set up total votes
        TextView totalLikes = findViewById(R.id.totalLikeDislikeCount);
        totalLikes.setText(String.valueOf(totalVotes));

        // Set up created date
        TextView createdDateTextView = findViewById(R.id.textCreatedAt);
        createdDateTextView.setText(createdDate);

        ImageButton mapButton = findViewById(R.id.iconOpenMap);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                // Open Google Maps with marker on the location of the post
                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + post.getPostContent() + ")");
                Log.d("Post", "Map URI: " + gmmIntentUri);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
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
            openCommentPage(postId);
        });

        ImageButton upVoteButton = findViewById(R.id.upVoteIcon);
        ImageButton downVoteButton = findViewById(R.id.downVoteIcon);
        TextView totalLikes = findViewById(R.id.totalLikeDislikeCount);
        upVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.isUpVotedByUser()) {
                    post.decrementUpVoteCount();
                    post.setUpVotedByUser(false);
                } else {
                    if (post.isDownVotedByUser()) {
                        post.incrementUpVoteCount();
                        post.decrementDownVoteCount();
                        post.setUpVotedByUser(true);
                        post.setDownVotedByUser(false);
                    } else {
                        post.incrementUpVoteCount();
                        post.setUpVotedByUser(true);
                    }
                }

                //Update icon both upVote and downVote
                upVoteButton.setImageResource(post.isUpVotedByUser() ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
                downVoteButton.setImageResource(post.isDownVotedByUser() ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);

                calculateTotalVotes(post, totalLikes);

                //Update database, add userNames to upVotedUsers array
                String postId = post.getPostId();
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                //Get upVotedUsers array from database where postId = postId
                database.collection("Posts").document(postId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the documentSnapshot from task
                        if (task.getResult() != null) {
                            // Get the upVotedUsers array from documentSnapshot
                            ArrayList<String> upVotedUsers = (ArrayList<String>) task.getResult().get("upVotedUsers");
                            // Get downVotedUsers array from documentSnapshot to check if userName is in downVotedUsers array
                            ArrayList<String> downVotedUsers = (ArrayList<String>) task.getResult().get("downVotedUsers");
                            controlUpVote(upVotedUsers, downVotedUsers, database, postId);
                        }
                    }
                });
            }
        });
        downVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.isDownVotedByUser()) {
                    post.decrementDownVoteCount();
                    post.setDownVotedByUser(false);
                } else {
                    if (post.isUpVotedByUser()) {
                        post.incrementDownVoteCount();
                        post.decrementUpVoteCount();
                        post.setUpVotedByUser(false);
                        post.setDownVotedByUser(true);
                    } else {
                        post.incrementDownVoteCount();
                        post.setDownVotedByUser(true);
                    }
                }

                //Update icon both upVote and downVote
                upVoteButton.setImageResource(post.isUpVotedByUser() ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
                downVoteButton.setImageResource(post.isDownVotedByUser() ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);

                calculateTotalVotes(post, totalLikes);

                //Update database, add userNames to downVotedUsers array
                String postId = post.getPostId();
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                //Get downVotedUsers array from database where postId = postId
                database.collection("Posts").document(postId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the documentSnapshot from task
                        if (task.getResult() != null) {
                            // Get the downVotedUsers array from documentSnapshot
                            ArrayList<String> downVotedUsers = (ArrayList<String>) task.getResult().get("downVotedUsers");
                            // Get upVotedUsers array from documentSnapshot to check if userName is in upVotedUsers array
                            ArrayList<String> upVotedUsers = (ArrayList<String>) task.getResult().get("upVotedUsers");
                            controlDownVote(downVotedUsers, upVotedUsers, database, postId);
                        }
                    }
                });
            }
        });
    }

    private void controlUpVote(ArrayList<String> upVotedUsers, ArrayList<String> downVotedUsers, FirebaseFirestore database, String postId) {
        if (upVotedUsers != null) {
            if (upVotedUsers.contains(userName)) {
                //Remove userName from upVotedUsers array
                upVotedUsers.remove(userName);
                //Update database
                database.collection("Posts").document(postId).update("upVotedUsers", upVotedUsers);
            } else {
                //Add userName to upVotedUsers array
                upVotedUsers.add(userName);
                //Update database
                database.collection("Posts").document(postId).update("upVotedUsers", upVotedUsers);
            }
        } else {
            //Create new upVotedUsers array
            ArrayList<String> newUpVotedUsers = new ArrayList<>();
            //Add userName to newUpVotedUsers array
            newUpVotedUsers.add(userName);
            //Update database
            database.collection("Posts").document(postId).update("upVotedUsers", newUpVotedUsers);
        }

        if (downVotedUsers != null) {
            if (downVotedUsers.contains(userName)) {
                //Remove userName from downVotedUsers array
                downVotedUsers.remove(userName);
                //Update database
                database.collection("Posts").document(postId).update("downVotedUsers", downVotedUsers);
            }
        }
    }

    private void controlDownVote(ArrayList<String> downVotedUsers, ArrayList<String> upVotedUsers, FirebaseFirestore database, String postId) {
        if (downVotedUsers != null) {
            if (downVotedUsers.contains(userName)) {
                //Remove userName from downVotedUsers array
                downVotedUsers.remove(userName);
                //Update database
                database.collection("Posts").document(postId).update("downVotedUsers", downVotedUsers);
            } else {
                //Add userName to downVotedUsers array
                downVotedUsers.add(userName);
                //Update database
                database.collection("Posts").document(postId).update("downVotedUsers", downVotedUsers);
            }
        } else {
            //Create new downVotedUsers array
            ArrayList<String> newDownVotedUsers = new ArrayList<>();
            //Add userName to newDownVotedUsers array
            newDownVotedUsers.add(userName);
            //Update database
            database.collection("Posts").document(postId).update("downVotedUsers", newDownVotedUsers);
        }

        if (upVotedUsers != null) {
            if (upVotedUsers.contains(userName)) {
                //Remove userName from upVotedUsers array
                upVotedUsers.remove(userName);
                //Update database
                database.collection("Posts").document(postId).update("upVotedUsers", upVotedUsers);
            }
        }
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
        TextView userNameTextView = findViewById(R.id.textUsername);
        userNameTextView.setText(post.getUserName());

        //Set post distance
        TextView distance = findViewById(R.id.textDistance);
        distance.setText(post.getDistanceFromUser());

        //Set post content
        TextView postContent = findViewById(R.id.textPost);
        postContent.setText(post.getPostContent());

        //Set post Media
        switch (post.getMediaType()) {
            case "image":
                imageUri = post.getMediaPath();
                StorageReference imageReference = storage.getReferenceFromUrl(imageUri);
                ImageView imagePost = findViewById(R.id.imagePost);
                imageReference.getBytes(1024 * 1024 * 100).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    bitmap = resizeBitmap(bitmap);
                    imagePost.setImageBitmap(bitmap);
                    imagePost.setVisibility(ImageView.VISIBLE);
                }).addOnFailureListener(e -> Log.d("Post", "Error getting image: " + e.getMessage()));
                break;
            case "audio":
                audioUri = post.getMediaPath();
                StorageReference audioReference = storage.getReferenceFromUrl(audioUri);
                View audioPost = findViewById(R.id.audioPlayerLayout);
                MaterialCardView audioCard = findViewById(R.id.audioPostCard);
                audioReference.getBytes(1024 * 1024 * 100).addOnSuccessListener(bytes -> {
                    audioCard.setVisibility(MaterialCardView.VISIBLE);
                    audioPost.setVisibility(View.VISIBLE);
                    ImageButton playPauseButton = findViewById(R.id.playPauseButton);
                    playPauseButton.setOnClickListener(v -> {
                        // Change play icon to pause icon
                        if (playPause) {
                            playPauseButton.setImageResource(R.drawable.ic_play);
                            pauseMediaPlayer();
                            playPause = false;
                        } else {
                            playPauseButton.setImageResource(R.drawable.ic_pause);
                            startMediaPlayer();
                            playPause = true;
                        }
                    });
                }).addOnFailureListener(e -> Log.d("Post", "Error getting audio: " + e.getMessage()));
                break;
            case "none":
                Log.d("Post", "No media");
                break;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Secure the aspect ratio
        int newWidth = 1080;
        int newHeight = (height * newWidth) / width;
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void openCommentPage(String postId) {
        Intent intent = new Intent(getApplicationContext(), CommentPage.class);
        intent.putExtra("postId", postId);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    private void startMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                // Get the download URL for the audio file
                storage.getReferenceFromUrl(audioUri)
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            try {
                                mediaPlayer.setDataSource(uri.toString());
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                // Set up SeekBar
                                SeekBar seekBar = findViewById(R.id.seekBar);
                                seekBar.setMax(mediaPlayer.getDuration());

                                // Update SeekBar progress and current duration
                                musicThread = new Thread(() -> {
                                    while (mediaPlayer != null) {
                                        try {
                                            if (mediaPlayer.isPlaying()) {
                                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                runOnUiThread(this::updateCurrentDuration);
                                                Thread.sleep(1000);
                                            } else {
                                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                runOnUiThread(this::updateCurrentDuration);
                                                Thread.sleep(1000);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                musicThread.start();

                                mediaPlayer.setOnCompletionListener(mp -> {
                                    // Playback completed actions
                                    ImageButton playPauseButton = findViewById(R.id.playPauseButton);
                                    playPauseButton.setImageResource(R.drawable.ic_play);
                                    playPause = false;
                                    stopMediaPlayer();
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .addOnFailureListener(e -> Log.d("Post", "Error getting audio URL: " + e.getMessage()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCurrentDuration() {
        TextView currentDurationTextView = findViewById(R.id.currentDuration);
        currentDurationTextView.setText(formatDuration(mediaPlayer.getCurrentPosition()));
    }

    @SuppressLint("DefaultLocale")
    private String formatDuration(int durationInMillis) {
        int seconds = durationInMillis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void pauseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                musicThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateTotalVotes(Post currentPost, TextView totalLikes) {
        int totalVotes = currentPost.getUpVoteCount() - currentPost.getDownVoteCount();
        totalLikes.setText(String.valueOf(totalVotes));
    }
}
