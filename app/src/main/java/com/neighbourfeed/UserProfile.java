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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;

import java.util.List;

public class UserProfile extends AppCompatActivity {

    TextView textViewUserName;
    String userName;
    String email;
    String uid;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        textViewUserName = findViewById(R.id.textViewUsername);
        textViewUserName.setText(intent.getStringExtra("userName"));
        userName = intent.getStringExtra("userName");
        email = intent.getStringExtra("email");
        uid = intent.getStringExtra("uid");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        LinearLayout linearLayoutPosts = findViewById(R.id.linearLayoutPosts);

        TextView headerTextView = findViewById(R.id.headerTextView);

        ImageButton profileButton = findViewById(R.id.profileButton);

        profileButton.setClickable(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            //Create a TextView object and display the selected button's text in the LinearLayout
            if (isChecked) {
                TextView textView = new TextView(getApplicationContext());
                if (checkedId == R.id.btnPosts) {
                    textView.setText("Posts");
                    db.collection("Posts")
                            .whereEqualTo("userName", userName)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                        String postContent = document.getString("content");

                                        TextView post = new TextView(getApplicationContext());
                                        post.setText(postContent);
                                        linearLayoutPosts.addView(post);
                                    }
                                } else {
                                    Log.d("TAG", "Error getting documents: ", task.getException());
                                }
                            });
                } else if (checkedId == R.id.btnComments) {
                    textView.setText("Comments");
                    db.collection("Comments")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot commentDoc : Objects.requireNonNull(task.getResult())) {
                                        List<HashMap<String, String>> comments = (List<HashMap<String, String>>) commentDoc.get("comments");

                                        if (comments != null) {
                                            for (HashMap<String, String> commentMap : comments) {
                                                String commentUserName = commentMap.get("userName");
                                                if (commentUserName != null && commentUserName.equals(userName)) {
                                                    String commentContent = commentMap.get("content");
                                                    if (commentContent != null) {
                                                        TextView commentTextView = new TextView(getApplicationContext());
                                                        commentTextView.setText(commentContent);
                                                        linearLayoutPosts.addView(commentTextView);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.d("UserProfile", "Error getting documents: ", task.getException());
                                }
                            });
                } else if (checkedId == R.id.btnUpVotes) {
                    textView.setText("Up Votes");
                } else if (checkedId == R.id.btnDownVotes) {
                    textView.setText("Down Votes");
                }
                //Add the TextView to the LinearLayout if the linearLayoutPosts is not empty then remove all the views
                if (linearLayoutPosts.getChildCount() > 0) {
                    linearLayoutPosts.removeAllViews();
                }
                linearLayoutPosts.addView(textView);
            }
        });
    }
}
