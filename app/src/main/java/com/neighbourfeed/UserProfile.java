package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        TextView textViewDisplay = findViewById(R.id.textViewDisplay);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnPosts) {
                    textViewDisplay.setText("Posts should be here");
                } else if (checkedId == R.id.btnComments) {
                    textViewDisplay.setText("Comments should be here");
                }
            }
        });
    }
}
