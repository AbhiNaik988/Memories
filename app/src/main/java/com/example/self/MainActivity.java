package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import util.JournalApi;

public class MainActivity extends AppCompatActivity {
    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Memories");


        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if(currentUser != null){
                currentUser = firebaseAuth.getCurrentUser();
                String currentUserId = currentUser.getUid();

                collectionReference.whereEqualTo("userId",currentUserId)
                        .addSnapshotListener((value, error) -> {
                            if(error != null){
                                return;
                            }
                            assert value != null;
                            if(!value.isEmpty()){
                                for(QueryDocumentSnapshot snapshot : value){
                                    JournalApi journalApi = JournalApi.getInstance();
                                    journalApi.setUserId(snapshot.getString("userId"));
                                    journalApi.setUserName(snapshot.getString("userName"));

                                    startActivity(new Intent(MainActivity.this,JournalListActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        };


        Button getStartedButton = findViewById(R.id.start_button);
        getStartedButton.setOnClickListener(view -> {
            //we go to login activity
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}