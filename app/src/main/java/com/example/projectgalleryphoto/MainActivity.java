package com.example.projectgalleryphoto;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton buttonUpload = findViewById(R.id.buttonUpload);

        db = FirebaseFirestore.getInstance();
        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(photoList, this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setAdapter(photoAdapter);

        buttonUpload.setOnClickListener(v -> openFileChooser());

        loadPhotos();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileReference = FirebaseStorage.getInstance().getReference("uploads")
                    .child(System.currentTimeMillis() + ".png");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Photo photo = new Photo(uri.toString()); // Create photo object with only URL
                                db.collection("photos").add(photo)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                                            loadPhotos(); // Load photos after successful upload
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to save photo to Firestore", Toast.LENGTH_SHORT).show());
                            }))
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPhotos() {
        db.collection("photos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                photoList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Photo photo = document.toObject(Photo.class);
                    photo.setId(document.getId()); // Set document ID to photo
                    photoList.add(photo);
                }
                photoAdapter.notifyDataSetChanged();
            }
        });
    }
}
