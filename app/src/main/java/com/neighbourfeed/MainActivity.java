package com.neighbourfeed;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageButton profileButton = findViewById(R.id.profileButton);
        ImageButton filterButton = findViewById(R.id.filterButton);
        ImageButton createPostButton = findViewById(R.id.createPostButton);

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

        showPosts();
    }

    private void openProfile() {
        Intent intent = new Intent(this, UserProfile.class);
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

    private void showPosts() {
        ListView listView = findViewById(R.id.postListView);

        ArrayList<Post> posts = new ArrayList<>();
        posts.add(new Post("John Doe", "1.2 km", "Content 1"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 2"));
        posts.add(new Post("John Doe", "1.2 km", "Content 3"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 4"));
        posts.add(new Post("John Doe", "1.2 km", "Content 5"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 6"));
        posts.add(new Post("John Doe", "1.2 km", "Content 7"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 8"));
        posts.add(new Post("John Doe", "1.2 km", "Content 9"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 10"));
        posts.add(new Post("John Doe", "1.2 km", "Content 11"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 12"));
        posts.add(new Post("John Doe", "1.2 km", "Content 13"));
        posts.add(new Post("Jane Doe", "1.5 km", "Content 14"));

        PostAdapter adapter = new PostAdapter(this, posts);
        listView.setAdapter(adapter);
    }

    private void openCreatePost() {
        Intent intent = new Intent(this, PostCreation.class);
        startActivity(intent);
    }
}