package com.neighbourfeed;

import android.content.Context;
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

        textUsername.setText(currentPost.getUserName());
        textDistance.setText(currentPost.getDistanceFromUser());
        textPost.setText(currentPost.getPostContent());
        imagePost.setImageResource(currentPost.getPostImage());
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
                //openComment();
            }
        });

        return listItemView;
    }

    private void calculateTotalVotes(Post currentPost, TextView totalLikes) {
        int totalVotes = currentPost.getUpVoteCount() - currentPost.getDownVoteCount();
        totalLikes.setText(String.valueOf(totalVotes));
    }
}

/*
if (isUpVotedByUser()) {
                    assert currentPost != null;
                    currentPost.decrementUpVoteCount();
                    setUpVotedByUser(false);
                } else {
                    if (isDownVotedByUser()) {
                        assert currentPost != null;
                        currentPost.incrementUpVoteCount();
                        currentPost.decrementDownVoteCount();
                        setUpVotedByUser(true);
                        setDownVotedByUser(false);
                    } else {
                        assert currentPost != null;
                        currentPost.incrementUpVoteCount();
                        setUpVotedByUser(true);
                    }
                }

                //Update icon both upVote and downVote
                upVoteButton.setImageResource(isUpVotedByUser ? R.drawable.arrow_up_bold : R.drawable.arrow_up_bold_outline);
                downVoteButton.setImageResource(isDownVotedByUser ? R.drawable.arrow_down_bold : R.drawable.arrow_down_bold_outline);

                calculateTotalVotes(currentPost, totalLikes);
                notifyDataSetChanged();
 */
