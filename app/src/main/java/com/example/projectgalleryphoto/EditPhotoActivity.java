package com.example.projectgalleryphoto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditPhotoActivity extends AppCompatActivity {
    private EditText editTextName, editTextDate, editTextCaption;
    private Button buttonUpdate;
    private FirebaseFirestore db;
    private String photoId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        editTextName = findViewById(R.id.editTextName);
        editTextDate = findViewById(R.id.editTextDate);
        editTextCaption = findViewById(R.id.editTextCaption);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        db = FirebaseFirestore.getInstance();

        photoId = getIntent().getStringExtra("photoId");

        // Load photo details from Firestore and populate EditText fields (you may want to implement this)
        loadPhotoDetails(photoId);

        buttonUpdate.setOnClickListener(v -> updatePhotoDetails());
    }

    private void loadPhotoDetails(String photoId) {
        // Implement loading the photo details based on photoId and set values to EditText fields
    }

    private void updatePhotoDetails() {
        String name = editTextName.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String caption = editTextCaption.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || caption.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Photo object with updated details
        Photo photo = new Photo(/* imageUrl */); // Replace with the actual image URL
        photo.setId(photoId); // Set the id for Firestore update
        photo.setName(name);
        photo.setDate(date);
        photo.setCaption(caption);

        db.collection("photos").document(photoId)
                .set(photo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Photo details updated", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update photo", Toast.LENGTH_SHORT).show());
    }
}
