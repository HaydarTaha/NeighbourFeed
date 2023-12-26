package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CommentPage extends AppCompatActivity {

    String postId;
    FirebaseFirestore database;
    ArrayList<Comment> comments;
    String userName;
    CommentAdapter adapter;
    double latitude;
    double longitude;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        userName = intent.getStringExtra("userName");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        Log.d("Comment", "onStart: " + postId);
        database = FirebaseFirestore.getInstance();
        fetchComments();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        ProgressBar progressBar = findViewById(R.id.progressBarComment);
        progressBar.setVisibility(View.VISIBLE);

        EditText commentEditText = findViewById(R.id.commentInputEditText);
        Button commentButton = findViewById(R.id.addCommentButton);

        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               // Not needed for this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this case
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Check if the EditText has text and enable/disable the button accordingly
                commentButton.setEnabled(!editable.toString().trim().isEmpty());
            }
        });

        commentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString();
            if (!commentText.isEmpty()) {
                addComment(commentText, postId);
                commentEditText.setText("");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void fetchComments() {
        try {
            CollectionReference commentsRef = database.collection("Comments");
            DocumentReference postRef = commentsRef.document(postId);

            postRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        ArrayList<Object> commentList = (ArrayList<Object>) document.get("comments");

                        if (commentList != null) {
                            comments = new ArrayList<>();
                            for (Object commentObj : commentList) {
                                if (commentObj instanceof Map) {
                                    Map<String, Object> commentMap = (Map<String, Object>) commentObj;
                                    String userName = (String) commentMap.get("userName");
                                    String content = (String) commentMap.get("content");
                                    Timestamp date = (Timestamp) commentMap.get("date");
                                    assert date != null;
                                    Date commentDate = date.toDate();
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    String dateString = sdf.format(commentDate);

                                    Comment comment = new Comment(userName, content, dateString, postId);
                                    comments.add(comment);
                                }
                            }

                            if (!comments.isEmpty()) {
                                ProgressBar progressBar = findViewById(R.id.progressBarComment);
                                progressBar.setVisibility(View.GONE);
                                adapter = new CommentAdapter(this, comments, false, userName, latitude, longitude);
                                ListView listView = findViewById(R.id.commentListView);
                                listView.setAdapter(adapter);
                            } else {
                                Log.d("Comment", "Comments list is empty");
                                ProgressBar progressBar = findViewById(R.id.progressBarComment);
                                progressBar.setVisibility(View.GONE);
                                TextView noComments = findViewById(R.id.commentEmptyView);
                                noComments.setVisibility(View.VISIBLE);
                                noComments.setText("No comments yet");
                            }
                        } else {
                            Log.d("Comment", "Comments field is null");
                            ProgressBar progressBar = findViewById(R.id.progressBarComment);
                            progressBar.setVisibility(View.GONE);
                            TextView noComments = findViewById(R.id.commentEmptyView);
                            noComments.setVisibility(View.VISIBLE);
                            noComments.setText("No comments yet");

                        }
                    } else {
                        Log.d("Comment", "No such document");
                        ProgressBar progressBar = findViewById(R.id.progressBarComment);
                        progressBar.setVisibility(View.GONE);
                        TextView noComments = findViewById(R.id.commentEmptyView);
                        noComments.setVisibility(View.VISIBLE);
                        noComments.setText("No comments yet");
                    }
                } else {
                    Log.d("Comment", "Error getting document: ", task.getException());
                }
            });
        } catch (Exception e) {
            Log.d("Comment", "Error getting documents: ", e);
        }
    }

    private void addComment(String commentText, String postId) {
        try {
            // Get the current date and time
            Timestamp timestamp = new Timestamp(new Date());
            String userName = this.userName;

            // Create a new comment map
            Map<String, Object> newComment = new HashMap<>();
            newComment.put("userName", userName);
            newComment.put("content", commentText);
            newComment.put("date", timestamp);

            CollectionReference commentsRef = database.collection("Comments");
            DocumentReference postRef = commentsRef.document(postId);

            postRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        ArrayList<Object> commentList = (ArrayList<Object>) document.get("comments");

                        if (commentList != null) {
                            commentList.add(newComment);
                            postRef.update("comments", commentList);
                            comments.add(new Comment(userName, commentText, timestamp.toDate().toString(), postId));
                            adapter.notifyDataSetChanged();
                            ListView listView = findViewById(R.id.commentListView);
                            listView.setSelection(adapter.getCount() - 1);
                        } else {
                            Log.d("Comment", "Comments field is null");
                        }
                    } else {
                        Log.d("Comment", "No such document");
                        createNewDocument(postId, userName, commentText, timestamp);
                    }
                } else {
                    Log.d("Comment", "Error getting document: ", task.getException());
                }
            });
        } catch (Exception e) {
            Log.d("Comment", "Error adding comment: " + e.getMessage());
        }
    }

    private void createNewDocument(String postId, String userName, String commentText, Timestamp timestamp) {
        try {
            Map<String, Object> commentMap = new HashMap<>();
            ArrayList<Object> commentList = new ArrayList<>();
            commentMap.put("userName", userName);
            commentMap.put("content", commentText);
            commentMap.put("date", timestamp);
            commentList.add(commentMap);
            Map<String, Object> newComment = new HashMap<>();
            newComment.put("comments", commentList);

            database.collection("Comments")
                    .document(postId)
                    .set(newComment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Comment", "DocumentSnapshot successfully written!");
                            TextView noComments = findViewById(R.id.commentEmptyView);
                            noComments.setVisibility(View.GONE);
                            // Create a new adapter and set it to the ListView
                            comments = new ArrayList<>();
                            comments.add(new Comment(userName, commentText, timestamp.toDate().toString(), postId));
                            adapter = new CommentAdapter(CommentPage.this, comments, false, userName, latitude, longitude);
                            ListView listView = findViewById(R.id.commentListView);
                            listView.setAdapter(adapter);
                        }
                    })
                    .addOnFailureListener(e -> Log.d("Comment", "Error writing document", e));
        } catch (Exception e) {
            Log.d("Comment", "Error creating new document: " + e.getMessage());
        }
    }
}
