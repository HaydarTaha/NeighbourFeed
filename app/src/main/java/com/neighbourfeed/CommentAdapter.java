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

public class CommentAdapter extends ArrayAdapter<Comment> {

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_comment, parent, false);
        }

        Comment currentComment = getItem(position);

        TextView commentUsername = listItemView.findViewById(R.id.commentUsername);
        TextView commentText = listItemView.findViewById(R.id.commentText);

        assert currentComment != null;
        commentUsername.setText(currentComment.getUserName());
        commentText.setText(currentComment.getCommentText());

        return listItemView;
    }
}
