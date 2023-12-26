package com.neighbourfeed;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class PostAdapter extends ArrayAdapter<Post> {

    String userName;

    public PostAdapter(Context context, ArrayList<Post> posts, String userName) {
        super(context, 0, posts);
        this.userName = userName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_post, parent, false);
        }

        Post currentPost = getItem(position);

        TextView textUsername = listItemView.findViewById(R.id.textUsername);
        TextView textDistance = listItemView.findViewById(R.id.textDistance);
        TextView textPost = listItemView.findViewById(R.id.textPost);
        ImageButton upVoteButton = listItemView.findViewById(R.id.upVoteIcon);
        ImageButton downVoteButton = listItemView.findViewById(R.id.downVoteIcon);
        ImageButton commentButton = listItemView.findViewById(R.id.iconComment);
        TextView commentCount = listItemView.findViewById(R.id.textCommentCount);
        TextView totalLikes = listItemView.findViewById(R.id.totalLikeDislikeCount);
        ImageButton mapButton = listItemView.findViewById(R.id.iconOpenMap);
        TextView textCreatedAt = listItemView.findViewById(R.id.textCreatedAt);

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert currentPost != null;
                onClickPost(currentPost);
            }
        });

        //Set upVote and downVote icon
        assert currentPost != null;
        upVoteButton.setImageResource(currentPost.isUpVotedByUser() ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
        downVoteButton.setImageResource(currentPost.isDownVotedByUser() ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);
        textUsername.setText(currentPost.getUserName());
        textDistance.setText(currentPost.getDistanceFromUser());
        textPost.setText(currentPost.getPostContent());
        commentCount.setText(String.valueOf(currentPost.getCommentCount()));
        calculateTotalVotes(currentPost, totalLikes);

        //Set createdAt text
        textCreatedAt.setText(currentPost.getPostDate());

        upVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPost.isUpVotedByUser()) {
                    currentPost.decrementUpVoteCount();
                    currentPost.setUpVotedByUser(false);
                } else {
                    if (currentPost.isDownVotedByUser()) {
                        currentPost.incrementUpVoteCount();
                        currentPost.decrementDownVoteCount();
                        currentPost.setUpVotedByUser(true);
                        currentPost.setDownVotedByUser(false);
                    } else {
                        currentPost.incrementUpVoteCount();
                        currentPost.setUpVotedByUser(true);
                    }
                }

                //Update icon both upVote and downVote
                upVoteButton.setImageResource(currentPost.isUpVotedByUser() ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
                downVoteButton.setImageResource(currentPost.isDownVotedByUser() ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);

                calculateTotalVotes(currentPost, totalLikes);
                notifyDataSetChanged();

                //Update database, add userNames to upVotedUsers array
                String postId = currentPost.getPostId();
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
                if (currentPost.isDownVotedByUser()) {
                    currentPost.decrementDownVoteCount();
                    currentPost.setDownVotedByUser(false);
                } else {
                    if (currentPost.isUpVotedByUser()) {
                        currentPost.incrementDownVoteCount();
                        currentPost.decrementUpVoteCount();
                        currentPost.setUpVotedByUser(false);
                        currentPost.setDownVotedByUser(true);
                    } else {
                        currentPost.incrementDownVoteCount();
                        currentPost.setDownVotedByUser(true);
                    }
                }

                //Update icon both upVote and downVote
                upVoteButton.setImageResource(currentPost.isUpVotedByUser() ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
                downVoteButton.setImageResource(currentPost.isDownVotedByUser() ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);

                calculateTotalVotes(currentPost, totalLikes);
                notifyDataSetChanged();

                //Update database, add userNames to downVotedUsers array
                String postId = currentPost.getPostId();
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

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCommentPage(currentPost.getPostId());
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Google Maps with marker on the location of the post
                Uri gmmIntentUri = Uri.parse("geo:" + currentPost.getLatitude() + "," + currentPost.getLongitude() + "?q=" + currentPost.getLatitude() + "," + currentPost.getLongitude() + "(" + currentPost.getPostContent() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                getContext().startActivity(mapIntent);
            }
        });

        return listItemView;
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

    private void openCommentPage(String postId) {
        Intent intent = new Intent(getContext(), CommentPage.class);
        intent.putExtra("postId", postId);
        intent.putExtra("userName", userName);
        getContext().startActivity(intent);
    }

    private void calculateTotalVotes(Post currentPost, TextView totalLikes) {
        int totalVotes = currentPost.getUpVoteCount() - currentPost.getDownVoteCount();
        totalLikes.setText(String.valueOf(totalVotes));
    }

    private void onClickPost(Post currentPost) {
        Intent intent = new Intent(getContext(), PostPage.class);
        intent.putExtra("postId", currentPost.getPostId());
        intent.putExtra("userName", userName);
        intent.putExtra("post", currentPost);
        intent.putExtra("isLiked", currentPost.isUpVotedByUser());
        intent.putExtra("isDisliked", currentPost.isDownVotedByUser());
        intent.putExtra("totalVotes", currentPost.getUpVoteCount() - currentPost.getDownVoteCount());
        intent.putExtra("createDate", currentPost.getPostDate());
        intent.putExtra("latitude", currentPost.getLatitude());
        intent.putExtra("longitude", currentPost.getLongitude());
        getContext().startActivity(intent);
    }
}
