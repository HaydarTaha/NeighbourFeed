package com.neighbourfeed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class PostAdapter extends ArrayAdapter<Post> {

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

        TextView textUsername = listItemView.findViewById(R.id.textUsername);
        TextView textDistance = listItemView.findViewById(R.id.textDistance);
        TextView textPost = listItemView.findViewById(R.id.textPost);
        ImageView imagePost = listItemView.findViewById(R.id.imagePost);
        ImageButton upVoteButton = listItemView.findViewById(R.id.upVoteIcon);
        ImageButton downVoteButton = listItemView.findViewById(R.id.downVoteIcon);
        ImageButton commentButton = listItemView.findViewById(R.id.iconComment);
        TextView commentCount = listItemView.findViewById(R.id.textCommentCount);
        TextView totalLikes = listItemView.findViewById(R.id.totalLikeDislikeCount);

        //Set upVote and downVote icon
        assert currentPost != null;
        upVoteButton.setImageResource(currentPost.isUpVotedByUser() ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
        downVoteButton.setImageResource(currentPost.isDownVotedByUser() ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);
        getImage(currentPost.getImagePath(), imagePost);
        textUsername.setText(currentPost.getUserName());
        textDistance.setText(currentPost.getDistanceFromUser());
        textPost.setText(currentPost.getPostContent());
        commentCount.setText(String.valueOf(currentPost.getCommentCount()));
        calculateTotalVotes(currentPost, totalLikes);

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
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCommentPage(currentPost.getPostId());
            }
        });

        return listItemView;
    }

    private void openCommentPage(String postId) {
        Intent intent = new Intent(getContext(), CommentPage.class);
        intent.putExtra("postId", postId);
        getContext().startActivity(intent);
    }

    private void calculateTotalVotes(Post currentPost, TextView totalLikes) {
        int totalVotes = currentPost.getUpVoteCount() - currentPost.getDownVoteCount();
        totalLikes.setText(String.valueOf(totalVotes));
    }

    private void getImage(String url, ImageView imagePost) {
        if (!Objects.equals(url, "image")) {
            //TODO: Get image from url
        } else {
            imagePost.setVisibility(View.GONE);
        }
    }
}
