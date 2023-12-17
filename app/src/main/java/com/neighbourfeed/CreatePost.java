package com.neighbourfeed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Objects;

public class CreatePost extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 104;
    private static final int CAMERA_REQUEST = 102;
    private static final int GALLERY_REQUEST = 103;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    private Button btnStartRecording;
    private Button buttonPick;
    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Button btnSelectImageCamera = findViewById(R.id.btnSelectImageCamera);
        Button btnSelectFromGallery = findViewById(R.id.btnSelectFromGallery);
        btnStartRecording = findViewById(R.id.btnStartRecording);
        registerResult();

        btnSelectFromGallery.setOnClickListener(view -> pickImage());
        EditText editTextPostText = findViewById(R.id.editTextPostText);
        Spinner spinnerPostType = findViewById(R.id.spinnerPostType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPostType.setAdapter(adapter);

        btnSelectImageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });



        btnStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecording();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            if (result.getResultCode() == RESULT_OK) {
                                Uri imageUri = result.getData().getData();
                                showToast("Image selected from gallery");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera();
        }
    }

   /* private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
        } else {
            openGallery();
        }
    }*/


    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST);
        } else {
            startRecording();
        }
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            requestAudioPermission();
        }
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/audio_record.3gp");
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
        isRecording = true;

        showToast("Audio recording started");
        updateButtonState();
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            showToast("Audio recording stopped");
            updateButtonState();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        resultLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("Camera permission is required to take a picture");
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //openGallery();
            } else {
                //showToast("Storage permission is required to select an image");
            }
        } else if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                showToast("Audio recording permission is required");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateButtonState() {
        if (isRecording) {
            btnStartRecording.setText("Stop Recording");
        } else {
            btnStartRecording.setText("Start Recording");
        }
    }
}
