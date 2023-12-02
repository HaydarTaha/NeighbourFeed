package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        LinearLayout linearLayoutPosts = findViewById(R.id.linearLayoutPosts);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            //Create a TextView object and display the selected button's text in the LinearLayout
            if (isChecked) {
                TextView textView = new TextView(getApplicationContext());
                if (checkedId == R.id.btnPosts) {
                    textView.setText("Posts");
                } else if (checkedId == R.id.btnComments) {
                    textView.setText("Comments");
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
