package com.neighbourfeed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class PostAdapter extends ArrayAdapter<Post> {

    private boolean isLiked;
    private boolean isDisliked;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        super(context, 0, posts);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_post, parent, false);
        }

        Post currentPost = getItem(position);

        //TODO: Get like and dislike status from database for current user but for now it is false
        isLiked = false;
        isDisliked = false;

        TextView textUsername = listItemView.findViewById(R.id.textUsername);
        TextView textDistance = listItemView.findViewById(R.id.textDistance);
        TextView textPost = listItemView.findViewById(R.id.textPost);
        ImageView imagePost = listItemView.findViewById(R.id.imagePost);
        ImageButton likeButton = listItemView.findViewById(R.id.iconLike);
        ImageButton dislikeButton = listItemView.findViewById(R.id.iconDislike);
        ImageButton commentButton = listItemView.findViewById(R.id.iconComment);
        TextView commentCount = listItemView.findViewById(R.id.textCommentCount);
        TextView totalLikes = listItemView.findViewById(R.id.totalLikeDislikeCount);

        //Update icon src for like button
        if (isLiked) {
            likeButton.setImageResource(R.drawable.arrow_up_bold);
        } else {
            likeButton.setImageResource(R.drawable.arrow_up_bold_outline);
        }

        //Update icon src for dislike button
        if (isDisliked) {
            dislikeButton.setImageResource(R.drawable.arrow_down_bold);
        } else {
            dislikeButton.setImageResource(R.drawable.arrow_down_bold_outline);
        }

        if (currentPost != null) {
            textUsername.setText(currentPost.getUserName());
            textDistance.setText(currentPost.getDistanceFromUser());
            textPost.setText(currentPost.getPostContent());
            imagePost.setImageResource(currentPost.getPostImage());
            commentCount.setText(String.valueOf(currentPost.getCommentCount()));
            totalLikes.setText(String.valueOf(currentPost.getLikeCount() - currentPost.getDislikeCount()));
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLike = true;
                //Control if user already liked or disliked
                if (isLiked && !isDisliked) {
                    isLiked = false;
                    //Update icon src
                    likeButton.setImageResource(R.drawable.arrow_up_bold_outline);

                    assert currentPost != null;
                    int newLikeCount = currentPost.getLikeCount() - 1;
                    currentPost.setLikeCount(newLikeCount);

                    updateTotalLikeCount(currentPost, totalLikes);

                    //TODO: Update database
                } else if (!isLiked && isDisliked) {
                    Toast.makeText(getContext(), "You can't like and dislike at the same time", Toast.LENGTH_SHORT).show();
                } else if (!isLiked && !isDisliked) {
                    isLiked = true;
                    //Update icon src
                    likeButton.setImageResource(R.drawable.arrow_up_bold);

                    assert currentPost != null;
                    int newLikeCount = currentPost.getLikeCount() + 1;
                    currentPost.setLikeCount(newLikeCount);

                    updateTotalLikeCount(currentPost, totalLikes);

                    //TODO: Update database
                }
            }
        });


        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLike = false;
                //Control if user already liked or disliked
                if (!isLiked && isDisliked) {
                    isDisliked = false;
                    //Update icon src
                    dislikeButton.setImageResource(R.drawable.arrow_down_bold_outline);

                    assert currentPost != null;
                    int newDislikeCount = currentPost.getDislikeCount() - 1;
                    currentPost.setDislikeCount(newDislikeCount);

                    updateTotalLikeCount(currentPost, totalLikes);
                } else if (isLiked && !isDisliked) {
                    Toast.makeText(getContext(), "You can't like and dislike at the same time", Toast.LENGTH_SHORT).show();
                } else if (!isDisliked && !isLiked) {
                    isDisliked = true;
                    //Update icon src
                    dislikeButton.setImageResource(R.drawable.arrow_down_bold);

                    assert currentPost != null;
                    int newDislikeCount = currentPost.getDislikeCount() + 1;
                    currentPost.setDislikeCount(newDislikeCount);

                    updateTotalLikeCount(currentPost, totalLikes);
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openComment();
            }
        });

        return listItemView;
    }

    private void updateTotalLikeCount(Post currentPost, TextView totalLikes) {
        int likeCount = currentPost.getLikeCount();
        int dislikeCount = currentPost.getDislikeCount();
        int totalLikeCount = likeCount - dislikeCount;
        totalLikes.setText(String.valueOf(totalLikeCount));
    }
}
