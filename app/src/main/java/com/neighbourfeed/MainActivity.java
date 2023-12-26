package com.neighbourfeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import android.widget.SeekBar;
import android.view.LayoutInflater;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


public class MainActivity extends AppCompatActivity {

    FirebaseFirestore database;
    User user;
    private List<String> selectedFilterTypes = new ArrayList<>();
    private int selectedDistance = 10;
    private GeoPoint userLocation;
    private double latitude;
    private double longitude;
    private boolean locationFound = false;

    @Override
    protected void onStart() {
        super.onStart();
        // Get intent data
        Intent intent = getIntent();

        // Get from login
        boolean fromLogin = intent.getBooleanExtra("fromLogin", false);

        // Get from register
        boolean fromRegister = intent.getBooleanExtra("fromRegister", false);

        // Get from user profile
        boolean fromUserProfile = intent.getBooleanExtra("fromUserProfile", false);

        database = FirebaseFirestore.getInstance();

        if (fromLogin || fromRegister) {
            String name = intent.getStringExtra("name");
            String email = intent.getStringExtra("email");
            String uid = intent.getStringExtra("uid");

            // Create user object
            user = new User(name, email, uid);

            // Find user location
            findUserLocation();
        } else if (fromUserProfile) {
            String userName = intent.getStringExtra("userName");
            String email = intent.getStringExtra("email");
            String uid = intent.getStringExtra("uid");

            // Create user object
            user = new User(userName, email, uid);

            // Find user location
            findUserLocation();
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

        profileButton.setEnabled(locationFound);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Profile button clicked");
                openProfile();
            }
        });

        filterButton.setEnabled(locationFound);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Filter button clicked");
                openFilter();
            }
        });

        createPostButton.setEnabled(locationFound);
        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Create post button clicked");
                openCreatePost();

            }
        });
    }

    private void openProfile() {
        Intent intent = new Intent(this, UserProfile.class);
        intent.putExtra("userName", user.getUserName());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("uid", user.getUid());
        intent.putExtra("latitude", userLocation.getLatitude());
        intent.putExtra("longitude", userLocation.getLongitude());
        startActivity(intent);
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
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedDistance = distanceSeekBar.getProgress();
                selectedFilterTypes.clear(); // Clear previous filters
                for (CheckBox checkbox : checkBoxes) {
                    if (checkbox.isChecked()) {
                        selectedFilterTypes.add(checkbox.getText().toString());
                    }
                }
                // Check if the user location is found
                if (locationFound) {
                    showPosts();
                } else {
                    findUserLocation();
                }
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
        PostAdapter adapter = new PostAdapter(this, posts, user.getUserName());
        listView.setAdapter(adapter);
    }

    private void fetchPosts(ArrayList<Post> posts) {
        TextView noPost = findViewById(R.id.postEmptyView);
        noPost.setVisibility(View.INVISIBLE);
        // Fetch posts from firebase database
        database.collection("Posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Process fetched posts and apply filters
                        for (int i = 0; i < task.getResult().size(); i++) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(i);
                            Post post = processPostData(document);
                            fetchCommentsWithID(document.getId(), post);

                            // Check if the post matches the selected filter types
                            if (selectedFilterTypes.isEmpty() || selectedFilterTypes.contains(post.getType())) {
                                // Check if the post is within the selected distance
                                double latitude = post.getLatitude();
                                double longitude = post.getLongitude();
                                GeoPoint postLocation = new GeoPoint(latitude, longitude);
                                double distance = calculateDistanceFromUser(postLocation);
                                if (distance <= selectedDistance) {
                                    if (post != null) {
                                        posts.add(post);
                                    }
                                }
                            }
                        }

                        // Update the UI after fetching and filtering posts
                        if (!posts.isEmpty()) {
                            ProgressBar progressBar = findViewById(R.id.progressBarMain);
                            progressBar.setVisibility(View.GONE);
                            PostAdapter adapter = new PostAdapter(this, posts, user.getUserName());
                            ListView listView = findViewById(R.id.postListView);
                            listView.setAdapter(adapter);
                        }else {
                            Log.d("Post", "Post list is empty");

                            noPost.setVisibility(View.VISIBLE);
                            noPost.setText("No post yet");
                        }
                    } else {
                        Log.d("MainActivity", "Error getting documents: ", task.getException());
                    }

                });
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
            if (upVotedUser.equals(user.getUserName())) {
                upVoted = true;
                break;
            }
        }
        boolean downVoted = false;
        for (String downVotedUser : downVotedUsers) {
            if (downVotedUser.equals(user.getUserName())) {
                downVoted = true;
                break;
            }
        }

        return new Post(userName, distanceString, content, upVotes, downVotes, 0, type, upVoted, downVoted, id, mediaType, mediaPath, createDate, locationGeoPoint.getLatitude(), locationGeoPoint.getLongitude());
    }

    private double calculateDistanceFromUser(GeoPoint location) {
        Log.d("MainActivity", "User location: " + userLocation.toString());
        Log.d("MainActivity", "Post location: " + location.toString());
        double dLat = Math.toRadians(location.getLatitude() - userLocation.getLatitude());
        double dLon = Math.toRadians(location.getLongitude() - userLocation.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLocation.getLatitude())) * Math.cos(Math.toRadians(location.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        final int R = 6371; // Radius of the Earth in kilometers
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
                                post.setCommentCount(commentList.size());
                                notifyAdapter();
                                Log.d("Comment", "Comment count: " + commentList.size());
                            }
                        }
                    } else {
                        Log.d("Comment", "Error getting document: ", task.getException());
                    }
                });
    }

    private void notifyAdapter() {
        ListView listView = findViewById(R.id.postListView);
        PostAdapter adapter = (PostAdapter) listView.getAdapter();
        adapter.notifyDataSetChanged();
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
        intent.putExtra("userName", user.getUserName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset filters
        selectedFilterTypes.clear();
        // Reset location found
        locationFound = false;
        // Remove all posts if any
        ListView listView = findViewById(R.id.postListView);
        PostAdapter adapter = (PostAdapter) listView.getAdapter();
        if (adapter != null) {
            adapter.clear();
        }
        // Fetch posts again
        ProgressBar progressBar = findViewById(R.id.progressBarMain);
        progressBar.setVisibility(View.VISIBLE);
        // Check if the user location is found
        if (locationFound) {
            showPosts();
        } else {
            findUserLocation();
        }
    }

    @SuppressLint("SetTextI18n")
    private void findUserLocation() {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(android.location.Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        userLocation = new GeoPoint(latitude, longitude);
                        userLocation = new GeoPoint(latitude, longitude);
                        Log.d("MainActivity", "User location: " + userLocation.toString());
                        locationFound = true;
                        // Stop listening for location updates
                        locationManager.removeUpdates(this);
                        // Enable filter button
                        ImageButton filterButton = findViewById(R.id.filterButton);
                        filterButton.setEnabled(true);
                        // Enable profile button
                        ImageButton profileButton = findViewById(R.id.profileButton);
                        profileButton.setEnabled(true);
                        // Enable create post button
                        ImageButton createPostButton = findViewById(R.id.createPostButton);
                        createPostButton.setEnabled(true);
                        // Show posts
                        showPosts();
                    }
                };
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            } else {
                Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
                ProgressBar progressBar = findViewById(R.id.progressBarMain);
                progressBar.setVisibility(View.GONE);
                TextView noPost = findViewById(R.id.postEmptyView);
                noPost.setText("Please enable location services");
                noPost.setVisibility(View.VISIBLE);
            }
        }
    }
}