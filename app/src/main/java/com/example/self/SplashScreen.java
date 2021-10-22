package com.example.self;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import util.JournalApi;

public class SplashScreen extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    sleep(2000);
                }
                catch (Exception e){
                    Log.d("TAG", "Error!");
                }
                finally {
                    if(currentUser != null){
                        startActivity(new Intent(SplashScreen.this,JournalListActivity.class));
                        finish();
                    }
                    else{
                        startActivity(new Intent(SplashScreen.this,MainActivity.class));
                        finish();
                    }

                }

            }
        };

        thread.start();
    }
}