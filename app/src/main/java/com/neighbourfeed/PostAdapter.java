package com.neighbourfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        if (currentPost != null) {
            textUsername.setText(currentPost.getUserName());
            textDistance.setText(currentPost.getDistanceFromUser());
            textPost.setText(currentPost.getPostContent());
            imagePost.setImageResource(currentPost.getPostImage());
        }

        return listItemView;
    }
}
