package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CommentPage extends AppCompatActivity {

    String postId;
    FirebaseFirestore database;
    ArrayList<Comment> comments;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
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
    }

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

                                    Comment comment = new Comment(userName, content, dateString);
                                    comments.add(comment);
                                }
                            }

                            if (!comments.isEmpty()) {
                                ProgressBar progressBar = findViewById(R.id.progressBarComment);
                                progressBar.setVisibility(View.GONE);
                                CommentAdapter adapter = new CommentAdapter(this, comments);
                                ListView listView = findViewById(R.id.postListView);
                                listView.setAdapter(adapter);
                            } else {
                                Log.d("Comment", "Comments list is empty");
                            }
                        } else {
                            Log.d("Comment", "Comments field is null");
                        }
                    } else {
                        Log.d("Comment", "No such document");
                    }
                } else {
                    Log.d("Comment", "Error getting document: ", task.getException());
                }
            });
        } catch (Exception e) {
            Log.d("Comment", "Error getting documents: ", e);
        }
    }
}
