package com.example.self;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private TextView currentUserTextView;
    private ImageView imageView;

    private String currentUserId;
    private String currentUserName;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //connection to firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Memories");
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.postProgressBar);
        titleEditText = findViewById(R.id.postTitleEt);
        thoughtsEditText = findViewById(R.id.postThoughtsEt);
        imageView = findViewById(R.id.postImageView);
        saveButton = findViewById(R.id.postSaveJournalButton);
        addPhotoButton = findViewById(R.id.postCameraButton);

        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
        }

        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null) {
                //users here
            } else {
                //user not here
            }
        };

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.postSaveJournalButton:
                //save journal
                saveJournal();
                break;

            case R.id.postCameraButton:
                //get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {
        String title = titleEditText.getText().toString().trim();
        String thoughts = thoughtsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) & imageUri != null) {
            //created a path to save images
            StorageReference filepath = storageReference
                    .child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds()); //my_image_8784783

            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            //1.create a journal object - (model)
                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThought(thoughts);
                            journal.setImageUrl(imageUrl);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUserName(currentUserName);
                            journal.setUserId(currentUserId);

                            //2.invoke out collectionReference
                            collectionReference.add(journal)
                                    .addOnSuccessListener(documentReference -> {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                                    });

                            //3.and save a journal instance
                        });

                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                    });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri); //show image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}