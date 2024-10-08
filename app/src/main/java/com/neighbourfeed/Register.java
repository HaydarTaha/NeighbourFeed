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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {
    TextInputEditText editTextMail, editTextPassword, editTextUserName;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView goToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).hide();
        editTextMail = findViewById(R.id.email_register);
        editTextUserName = findViewById(R.id.username_register);
        editTextPassword = findViewById(R.id.password_register);
        buttonReg = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar_register);
        goToLogin = findViewById(R.id.loginNow);

        editTextPassword.setInputType(129);

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, userName, password;
                email = String.valueOf(editTextMail.getText());
                userName = String.valueOf(editTextUserName.getText());
                password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter Your Mail!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter Password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(Register.this, "Enter Username!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName)
                                            .build();
                                    assert user != null;
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        createUserNameInDatabase(userName, email);
                                                        Toast.makeText(Register.this, "Your account has been created", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        intent.putExtra("fromRegister", true);
                                                        intent.putExtra("name", userName);
                                                        intent.putExtra("email", email);
                                                        intent.putExtra("uid", user.getUid());
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void createUserNameInDatabase(String name, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = Map.of(
                "userName", name,
                "email", email
        );
        // Add a new document with a random ID
        db.collection("Users")
                .add(user)
                .addOnSuccessListener(documentReference -> Log.d("Register", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Register", "Error adding document", e));
    }
}