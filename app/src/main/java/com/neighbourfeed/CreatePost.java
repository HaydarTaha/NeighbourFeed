package com.neighbourfeed;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Objects;

public class CreatePost extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Button btnSelectImageCamera = findViewById(R.id.btnSelectImageCamera);
        Button btnSelectFromGallery = findViewById(R.id.btnSelectFromGallery);
        Button btnAddPost = findViewById(R.id.btnAddPost);
        EditText editTextPostText = findViewById(R.id.editTextPostText);
        Spinner spinnerPostType = findViewById(R.id.spinnerPostType);

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
