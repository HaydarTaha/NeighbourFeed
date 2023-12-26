package com.neighbourfeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreatePost extends AppCompatActivity {

    //CONSTANTS
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;

    //VARIABLES
    private String userName;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String mediaUri;
    private String mediaType = "none";
    private boolean isMedia = false;
    private MediaRecorder recorder = new MediaRecorder();
    private MediaPlayer mediaPlayer;
    private boolean playPause = false;
    private boolean isPlaying = false;
    private Thread musicThread;
    private double latitude;
    private double longitude;
    private String imageSource;
    private boolean locationIsFound = false;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        controlLocation();
        Log.d("CreatePost", "onStart: " + userName);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        EditText postText = findViewById(R.id.postText);
        Spinner postType = findViewById(R.id.postType);
        Button addPost = findViewById(R.id.addPost);
        Button takePhoto = findViewById(R.id.selectImageCamera);
        Button selectImage = findViewById(R.id.selectImageGallery);
        Button recordAudio = findViewById(R.id.startRecording);

        // Type of post
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.filter_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postType.setAdapter(adapter);

        // Control the postText for enabling/disabling the addPost button
        postText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this case
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Check if the EditText has text and enable/disable the button accordingly
                if (locationIsFound) {
                    addPost.setEnabled(!editable.toString().trim().isEmpty());
                }
            }
        });

        takePhoto.setOnClickListener(v -> {
            if (isMedia) {
                // Set the text of a button to "Take Photo"
                takePhoto.setText("Take Photo");

                // Enable other UI elements (buttons for selecting an image and recording audio)
                selectImage.setEnabled(true);
                recordAudio.setEnabled(true);

                // Hide an ImageView (presumably displaying the added image)
                ImageView imageView = findViewById(R.id.postAddImage);
                imageView.setVisibility(View.GONE);

                // Update the boolean flag to indicate that no image is added
                isMedia = false;

                // Delete the file
                File file = new File(Objects.requireNonNull(mediaUri));
                boolean deleted = file.delete();

                imageSource = null;

                mediaType = null;

                // Return from the method (this might not be necessary depending on the context)
                return;
            }

            try {
                mediaType = "image";
                imageSource = "camera";
                takePhoto();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        selectImage.setOnClickListener(v -> {
            if (isMedia) {
                // Set the text of a button to "Select Image"
                selectImage.setText("Select Image");

                // Enable other UI elements (buttons for taking a photo and recording audio)
                takePhoto.setEnabled(true);
                recordAudio.setEnabled(true);

                // Hide an ImageView (presumably displaying the added image)
                ImageView imageView = findViewById(R.id.postAddImage);
                imageView.setVisibility(View.GONE);

                // Update the boolean flag to indicate that no image is added
                isMedia = false;

                mediaType = null;
                imageSource = null;

                // Return from the method (this might not be necessary depending on the context)
                return;
            }
            mediaType = "image";
            imageSource = "gallery";
            selectImage();
        });

        recordAudio.setOnClickListener(v -> {
            String text = recordAudio.getText().toString();
            switch (text) {
                case "Start Recording":
                    recordAudio.setText("Stop Recording");
                    recordAudio();
                    selectImage.setEnabled(false);
                    takePhoto.setEnabled(false);
                    break;
                case "Stop Recording": {
                    mediaType = "audio";
                    stopRecording();
                    Log.d("CreatePost", "onCreate: " + mediaUri);
                    recordAudio.setText("Remove Audio");
                    MaterialCardView postAddAudio = findViewById(R.id.postAddAudio);
                    postAddAudio.setVisibility(View.VISIBLE);
                    break;
                }
                case "Remove Audio": {
                    mediaType = null;
                    mediaUri = null;
                    recordAudio.setText("Start Recording");
                    MaterialCardView postAddAudio = findViewById(R.id.postAddAudio);
                    postAddAudio.setVisibility(View.GONE);
                    selectImage.setEnabled(true);
                    takePhoto.setEnabled(true);
                    break;
                }
            }
        });

        ImageButton playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(v -> {
                    //Change play icon to pause icon
                    if (playPause) {
                        playPauseButton.setImageResource(R.drawable.ic_play);
                        playPause = false;
                        //Pause audio
                        pauseMediaPlayer();
                    } else {
                        playPauseButton.setImageResource(R.drawable.ic_pause);
                        playPause = true;
                        //Play audio
                        startMediaPlayer();
                    }
                });

        // Add a listener to addPost button, to control the location is location is found, if not, disable the button, after location is found, enable the button
        addPost.setEnabled(locationIsFound);
        addPost.setOnClickListener(v -> addPost());
    }

    private void takePhoto() throws IOException {
        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, start the camera capture
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            // Create the file where the photo should go
            File photoFile = createImageFile();
            // Get the Uri for the file
            Uri photoURI = FileProvider.getUriForFile(this, "com.neighbourfeed.fileprovider", photoFile);
            // Add the Uri to the intent
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            // Start the camera activity
            startActivityForResult(cameraIntent, CAMERA_CAPTURE_REQUEST_CODE);
        }
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Get the directory for the app's private pictures directory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Create the file
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        // Save the file path
        mediaUri = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CreatePost", "onActivityResult: " + requestCode + " " + resultCode);
        // Check if the request code is for camera capture
        if (requestCode == CAMERA_CAPTURE_REQUEST_CODE) {
            // Check if the result is successful
            if (resultCode == RESULT_OK) {
                // Image capture successful
                ImageView imageView = findViewById(R.id.postAddImage);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageURI(Uri.parse(mediaUri));
                isMedia = true;
                Button takePhoto = findViewById(R.id.selectImageCamera);
                takePhoto.setText("Remove Photo");
                Button selectImage = findViewById(R.id.selectImageGallery);
                selectImage.setEnabled(false);
                Button recordAudio = findViewById(R.id.startRecording);
                recordAudio.setEnabled(false);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mediaUri = Objects.requireNonNull(data.getData()).toString();
                Log.d("CreatePost", "onActivityResult: " + mediaUri);
                ImageView imageView = findViewById(R.id.postAddImage);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageURI(Uri.parse(mediaUri));
                isMedia = true;
                Button selectImage = findViewById(R.id.selectImageGallery);
                selectImage.setText("Remove Photo");
                Button takePhoto = findViewById(R.id.selectImageCamera);
                takePhoto.setEnabled(false);
                Button recordAudio = findViewById(R.id.startRecording);
                recordAudio.setEnabled(false);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image selection
            } else {
                // Image selection failed
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {

        }
    }

    @SuppressLint("IntentReset")
    private void selectImage() {
        // Start the gallery activity
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Choose only images and just one image
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), GALLERY_PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    private void recordAudio() {
        // Check if the audio permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            // Permission is already granted, start the audio recording
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String audioFileName = "3GP_" + timeStamp + "_";
            // Get the directory for the app's private pictures directory
            File storageDir = null;
            // storageDir to directory Recordings
            storageDir = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
            // Create the file
            File audioFile = null;
            try {
                audioFile = File.createTempFile(audioFileName, ".3gp", storageDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Save the file path
            assert audioFile != null;
            mediaUri = audioFile.getAbsolutePath();
            recorder.setOutputFile(mediaUri);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void startMediaPlayer() {
        if (mediaUri != null) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(mediaUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    // Set up SeekBar
                    SeekBar seekBar = findViewById(R.id.seekBar);
                    seekBar.setMax(mediaPlayer.getDuration());

                    // Update SeekBar progress and current duration
                    musicThread = new Thread(() -> {
                        while (mediaPlayer != null) {
                            try {
                                if (mediaPlayer.isPlaying()) {
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    runOnUiThread(this::updateCurrentDuration);
                                    Thread.sleep(1000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    musicThread.start();

                    mediaPlayer.setOnCompletionListener(mp -> {
                        // Playback completed actions
                        ImageButton playPauseButton = findViewById(R.id.playPauseButton);
                        playPauseButton.setImageResource(R.drawable.ic_play);
                        playPause = false;
                        stopMediaPlayer();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                mediaPlayer.start();
            }
        } else {
            Log.d("CreatePost", "playAudio: No audio to play");
        }
    }

    private void pauseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                musicThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCurrentDuration() {
        TextView currentDurationTextView = findViewById(R.id.currentDuration);
        currentDurationTextView.setText(formatDuration(mediaPlayer.getCurrentPosition()));
    }

    @SuppressLint("DefaultLocale")
    private String formatDuration(int durationInMillis) {
        int seconds = durationInMillis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void controlLocation() {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            // Permission is already granted, start the location service
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // GPS is enabled
                Log.d("CreatePost", "controlLocation: GPS is enabled");
                // Show a toast message
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        Log.d("CreatePost", "onLocationChanged: " + location.toString());
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Toast.makeText(CreatePost.this, "Location updated. Latitude: " + latitude + " Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                        locationIsFound = true;
                        Button addPost = findViewById(R.id.addPost);
                        addPost.setEnabled(!((EditText) findViewById(R.id.postText)).getText().toString().trim().isEmpty());
                        // Stop the location service
                        locationManager.removeUpdates(this);
                    }
                };
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                // GPS is disabled
                Log.d("CreatePost", "controlLocation: GPS is disabled");
                // Show a toast message
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addPost() {
        try {
            database = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            // Get the post text
            EditText postText = findViewById(R.id.postText);
            String content = postText.getText().toString();
            // Get the post type
            Timestamp timestamp = new Timestamp(new Date());
            ArrayList<String> downVotedUsers = new ArrayList<>();
            GeoPoint geoPoint = new GeoPoint(latitude, longitude);
            String mediaPath = null;
            if (mediaUri != null) {
                mediaPath = "mediaPath";
            } else {
                mediaPath = "none";
            }
            String mediaType;
            if (mediaUri != null) {
                if (this.mediaType.equals("image")) {
                    mediaType = "image";
                } else if (this.mediaType.equals("audio")) {
                    mediaType = "audio";
                } else {
                    mediaType = "none";
                }
            } else {
                mediaType = "none";
            }
            String type = ((Spinner) findViewById(R.id.postType)).getSelectedItem().toString();
            ArrayList<String> upVotedUsers = new ArrayList<>();
            String userName = this.userName;
            Map<String, Object> post = new HashMap<String, Object>();
            post.put("content", content);
            post.put("createDate", timestamp);
            post.put("downVotedUsers", downVotedUsers);
            post.put("location", geoPoint);
            post.put("mediaPath", mediaPath);
            post.put("mediaType", mediaType);
            post.put("type", type);
            post.put("upVotedUsers", upVotedUsers);
            post.put("userName", userName);
            database = FirebaseFirestore.getInstance();
            database.collection("Posts").add(post).addOnSuccessListener(documentReference -> {
                Log.d("CreatePost", "DocumentSnapshot added with ID: " + documentReference.getId());
                if (mediaUri != null) {
                    StorageReference storageRef = storage.getReference();
                    if (mediaType.equals("image")) {
                        if (imageSource.equals("camera")) {
                            String imagePath = "Posts/" + documentReference.getId() + "/image.jpg";
                            StorageReference imageRef = storageRef.child(imagePath);
                            Uri file = Uri.fromFile(new File(mediaUri));
                            imageRef.putFile(file)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        Log.d("CreatePost", "Image uploaded successfully");
                                        String remoteUri = "gs://neighbourfeed.appspot.com/Posts/" + documentReference.getId() + "/image.jpg";
                                        database.collection("Posts").document(documentReference.getId()).update("mediaPath", remoteUri);
                                        Toast.makeText(CreatePost.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Log.d("CreatePost", "Error uploading image", e));
                        } else if (imageSource.equals("gallery")) {
                            String imagePath = "Posts/" + documentReference.getId() + "/image.jpg";
                            StorageReference imageRef = storageRef.child(imagePath);
                            Uri file = Uri.parse(mediaUri);
                            imageRef.putFile(file)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        Log.d("CreatePost", "Image uploaded successfully");
                                        String remoteUri = "gs://neighbourfeed.appspot.com/Posts/" + documentReference.getId() + "/image.jpg";
                                        database.collection("Posts").document(documentReference.getId()).update("mediaPath", remoteUri);
                                        Toast.makeText(CreatePost.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Log.d("CreatePost", "Error uploading image", e));
                        }
                    } else if (mediaType.equals("audio")) {
                        String audioPath = "Posts/" + documentReference.getId() + "/audio.3gp";
                        StorageReference audioRef = storageRef.child(audioPath);
                        Uri file = Uri.fromFile(new File(mediaUri));
                        audioRef.putFile(file)
                                .addOnSuccessListener(taskSnapshot -> {
                                    Log.d("CreatePost", "Audio uploaded successfully");
                                    String remoteUri = "gs://neighbourfeed.appspot.com/Posts/" + documentReference.getId() + "/audio.3gp";
                                    database.collection("Posts").document(documentReference.getId()).update("mediaPath", remoteUri);
                                    Toast.makeText(CreatePost.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Log.d("CreatePost", "Error uploading audio", e));
                    }
                } else {
                    Toast.makeText(CreatePost.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(e -> Log.d("CreatePost", "Error adding document", e));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

