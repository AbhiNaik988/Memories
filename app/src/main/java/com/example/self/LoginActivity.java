package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 5001 ;
    private AutoCompleteTextView emailAddress;
    private EditText password;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Memories");
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress);
        Button loginButton = findViewById(R.id.email_sign_in_button);
        Button createAccButton = findViewById(R.id.create_acc_login_button);
        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);
        ImageButton googleButton = findViewById(R.id.googleSignInButton);
        ImageButton facebookButton = findViewById(R.id.facebookSignInButton);
        ImageButton githubButton = findViewById(R.id.githubSignInButton);

        createAccButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            finish();
        });

        loginButton.setOnClickListener(view -> {
            loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                    password.getText().toString().trim());
        });

        googleButton.setOnClickListener(view -> {
            Toast.makeText(LoginActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
        });

        facebookButton.setOnClickListener(view -> {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        });

        githubButton.setOnClickListener(view -> {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    

    private void loginEmailPasswordUser(String email, String pwd) {
        progressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
            mAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(task -> {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        String currentUserId = user.getUid();

                        collectionReference
                                .whereEqualTo("userId", currentUserId)
                                .addSnapshotListener((value, error) -> {

                                    assert value != null;
                                    if (!value.isEmpty()) {

                                        progressBar.setVisibility(View.GONE);

                                        for (QueryDocumentSnapshot snapshot : value) {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserName(snapshot.getString("userName"));
                                            journalApi.setUserId(snapshot.getString("userId"));

                                            //go to list activity
                                            startActivity(new Intent(LoginActivity.this,
                                                    PostJournalActivity.class));
                                            finish();
                                        }

                                    }

                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please Enter Email And Password", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this,JournalListActivity.class));
            finish();
        }
    }
}