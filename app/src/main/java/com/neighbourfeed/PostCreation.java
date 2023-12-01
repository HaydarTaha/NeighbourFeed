package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

// PostActivity.java
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Objects;

public class PostCreation extends AppCompatActivity {

    private Button btnSelectImageCamera, btnSelectFromGallery, btnAddPost;
    private EditText editTextPostText;
    private Spinner spinnerPostType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation);
        Objects.requireNonNull(getSupportActionBar()).hide();


        btnSelectImageCamera = findViewById(R.id.btnSelectImageCamera);
        btnSelectFromGallery = findViewById(R.id.btnSelectFromGallery);
        btnAddPost = findViewById(R.id.btnAddPost);
        editTextPostText = findViewById(R.id.editTextPostText);
        spinnerPostType = findViewById(R.id.spinnerPostType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPostType.setAdapter(adapter);

        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
