package com.neighbourfeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Login extends AppCompatActivity {

    TextInputEditText editTextMail, editTextPassword;
    Button buttonLog;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView goToRegister;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            for (UserInfo profile : currentUser.getProviderData()) {
                String providerId = profile.getProviderId();
                String uid = profile.getUid();
                String name = profile.getDisplayName();
                String email = profile.getEmail();

                if (providerId.equals("firebase")) {
                    Log.d("Login", "onStart: " + "Google user");
                    Log.d("Login", "onStart: " + "Name: " + name);
                    Log.d("Login", "onStart: " + "Email: " + email);
                    Log.d("Login", "onStart: " + "Uid: " + uid);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("fromLogin", true);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        } else {
            Log.d("Login", "onStart: " + "No user");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).hide();
        editTextMail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLog = findViewById(R.id.btn_login);

        editTextPassword.setInputType(129);

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        goToRegister = findViewById(R.id.registerNow);
        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });
        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(editTextMail.getText());
                password = String.valueOf(editTextPassword.getText());
                boolean isEmail = email.contains("@");

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter Your Mail!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter Password!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}