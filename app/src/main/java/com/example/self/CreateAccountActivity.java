package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {
    //Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //FireStore connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private EditText userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        Objects.requireNonNull(getSupportActionBar()).setTitle("Memories");
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        Button createAccountButton = findViewById(R.id.create_acc_button);
        progressBar = findViewById(R.id.create_progress);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);
        userNameEditText = findViewById(R.id.username_account);

        createAccountButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(emailEditText.getText().toString())
                    && !TextUtils.isEmpty(passwordEditText.getText().toString())
                    && !TextUtils.isEmpty(userNameEditText.getText().toString())) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = userNameEditText.getText().toString().trim();
                createUserEmailAccout(email, password, username);
            } else {
                Toast.makeText(this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void createUserEmailAccout(String email, String password, String username) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", currentUserId);
                            userObj.put("userName", username);

                            collectionReference.add(userObj)
                                    .addOnSuccessListener(documentReference -> {
                                        documentReference.get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (Objects.requireNonNull(task1.getResult()).exists()) {
                                                        progressBar.setVisibility(View.GONE);
                                                        String name = task1.getResult()
                                                                .getString("userName");

                                                        JournalApi journalApi = JournalApi.getInstance(); //Global API
                                                        journalApi.setUserId(currentUserId);
                                                        journalApi.setUserName(name);

                                                        Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                        intent.putExtra("userName", name);
                                                        intent.putExtra("userId", currentUserId);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            //something went wrong
                            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Fill all field properly.", Toast.LENGTH_SHORT).show();
        }
    }
}