package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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
            openCommentPage(postId);
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
}
