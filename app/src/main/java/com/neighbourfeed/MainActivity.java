package com.neighbourfeed;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.widget.SeekBar;
import android.view.LayoutInflater;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    FirebaseFirestore database;
    User user;

    @Override
    protected void onStart() {
        super.onStart();
        // Get intent data
        Intent intent = getIntent();

        // Get from login
        boolean fromLogin = intent.getBooleanExtra("fromLogin", false);

        // Get from register
        boolean fromRegister = intent.getBooleanExtra("fromRegister", false);

        if (fromLogin || fromRegister) {
            String name = intent.getStringExtra("name");
            String email = intent.getStringExtra("email");
            String uid = intent.getStringExtra("uid");

            // Create user object
            user = new User(name, email, uid);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageButton profileButton = findViewById(R.id.profileButton);
        ImageButton filterButton = findViewById(R.id.filterButton);
        ImageButton createPostButton = findViewById(R.id.createPostButton);

        ProgressBar progressBar = findViewById(R.id.progressBarMain);
        progressBar.setVisibility(View.VISIBLE);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Profile button clicked");
                openProfile();
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Filter button clicked");
                openFilter();
            }
        });

        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Create post button clicked");
                openCreatePost();

            }
        });

        database = FirebaseFirestore.getInstance();
        showPosts();
    }

    private void openProfile() {
        Intent intent = new Intent(this, UserProfile.class);
        intent.putExtra("userName", user.getUserName());
        startActivity(intent);
        finish();
    }

    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    private void openFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Options");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_filter_dialog, null);
        builder.setView(dialogView);

        SeekBar distanceSeekBar = dialogView.findViewById(R.id.distanceSeekBar);
        TextView distanceValueTextView = dialogView.findViewById(R.id.distanceValueTextView);
        distanceValueTextView.setText(distanceSeekBar.getProgress() + " km");

        LinearLayout checkboxContainer = dialogView.findViewById(R.id.checkboxContainer);

        // Checkbox names using R.array.filter_types
        String[] checkboxNames = getResources().getStringArray(R.array.filter_types);
        ArrayList<LinearLayout> checkboxRows = new ArrayList<>();
        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
        ArrayList<LinearLayout> checkBoxLinearLayouts = new ArrayList<>();

        // Calculate number of rows needed, if its odd add 1, if its even add 0
        int rowsNeeded = checkboxNames.length % 2 == 0 ? checkboxNames.length / 2 : (checkboxNames.length / 2) + 1;

        // Create checkboxes and add them to the rows, one Left and one Right
        for (int i = 0; i < checkboxNames.length; i++) {
            checkBoxes.add(new CheckBox(this));
            checkBoxes.get(i).setText(checkboxNames[i]);
            checkBoxes.get(i).setTextSize(18);
            checkBoxes.get(i).setPadding(8, 8, 8, 8);

            boolean isLeft = i % 2 == 0;
            if (isLeft) {
                checkBoxes.get(i).setGravity(Gravity.LEFT);
            } else {
                checkBoxes.get(i).setGravity(Gravity.RIGHT);
            }
        }

        // Create rows and add them to the container
        for (int i = 0; i < rowsNeeded; i++) {
            checkboxRows.add(new LinearLayout(this));
            checkboxRows.get(i).setOrientation(LinearLayout.HORIZONTAL);
        }

        // Add checkboxes to the rows
        for (int i = 0; i < checkboxNames.length; i++) {
            boolean isLeft = i % 2 == 0;
            if (isLeft) {
                checkboxRows.get(i / 2).addView(checkBoxes.get(i));
            } else {
                checkboxRows.get(i / 2).addView(checkBoxes.get(i));
            }
        }

        for (LinearLayout row : checkboxRows) {
            checkboxContainer.addView(row);
        }

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceValueTextView.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedDistance = distanceSeekBar.getProgress();
                StringBuilder selectedTypes = new StringBuilder();

                for (CheckBox checkbox : checkBoxes) {
                    if (checkbox.isChecked()) {
                        selectedTypes.append(checkbox.getText()).append(", ");
                    }
                }

                Toast.makeText(MainActivity.this, "Distance: " + selectedDistance + " km, Types: " + selectedTypes, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Filter canceled", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showPosts() {
        ListView listView = findViewById(R.id.postListView);
        ArrayList<Post> posts = new ArrayList<>();
        fetchPosts(posts);
        if (!posts.isEmpty()) {
            ProgressBar progressBar = findViewById(R.id.progressBarMain);
            progressBar.setVisibility(View.GONE);
        }
        PostAdapter adapter = new PostAdapter(this, posts);
        listView.setAdapter(adapter);
    }

    private void fetchPosts(ArrayList<Post> posts) {
        //TODO: Fetch posts from firebase database
        database.collection("Posts")
                .get()
                .addOnCompleteListener(task -> {
                    // Get top 10 posts from database if not empty and size is greater than 10
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty() && task.getResult().size() > 10) {
                            for (int i = 0; i < 10; i++) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(i);
                                Post post = processPostData(document);
                                Log.d("MainActivity", "Post: " + post.toString());
                                posts.add(post);
                            }
                        } else {
                            for (int i = 0; i < task.getResult().size(); i++) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(i);
                                Post post = processPostData(document);
                                Log.d("MainActivity", "Post: " + post.toString());
                                posts.add(post);
                            }
                        }
                        //After fetching posts, update the list view in the UI
                        if (!posts.isEmpty()) {
                            ProgressBar progressBar = findViewById(R.id.progressBarMain);
                            progressBar.setVisibility(View.GONE);
                            PostAdapter adapter = new PostAdapter(this, posts);
                            ListView listView = findViewById(R.id.postListView);
                            listView.setAdapter(adapter);
                        }
                    } else {
                        Log.d("MainActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private Post processPostData(DocumentSnapshot document) {
        String content = document.getString("content");
        String createDate = Objects.requireNonNull(document.getTimestamp("createDate")).toString();
        List<String> downVotedUsers = castObjectToList(document.get("downVotedUsers"));
        String imagePath = document.getString("imagePath");
        String location = Objects.requireNonNull(document.getGeoPoint("location")).toString();
        String type = document.getString("type");
        List<String> upVotedUsers = castObjectToList(document.get("upVotedUsers"));
        String userName = document.getString("userName");

        int upVotes = upVotedUsers.size();
        int downVotes = downVotedUsers.size();

        //TODO: Calculate distance from user
        double distance = 0.0;
        //calculateDistanceFromUser(location);
        distance = Math.round(distance * 10.0) / 10.0;
        String distanceString = Double.toString(distance);

        //TODO: Check if user has upVoted or downVoted
        boolean hasUpVoted = false;
        boolean hasDownVoted = false;

        //TODO: Check the commentCount
        int commentCount = 0;

        assert imagePath != null;
        if (imagePath.equals("imagePath")) {
            return new Post(userName, distanceString, content, upVotes, downVotes, commentCount, type, hasUpVoted, hasDownVoted);
        } else {
            return new Post(userName, distanceString, content, imagePath, upVotes, downVotes, commentCount, type, hasUpVoted, hasDownVoted);
        }
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

    private void openCreatePost() {
        Intent intent = new Intent(this, CreatePost.class);
        startActivity(intent);
    }
}