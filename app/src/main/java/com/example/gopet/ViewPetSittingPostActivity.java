package com.example.gopet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class ViewPetSittingPostActivity extends AppCompatActivity {

    private TextView textViewDate, textViewLocation, textViewNotes, textViewAnimals;
    private Button btnRequest;
    private FirebaseFirestore db;
    private String postId, ownerId;
    private PetSittingPost post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pet_sitting_post);

        textViewDate = findViewById(R.id.textViewDate);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewNotes = findViewById(R.id.textViewNotes);
        textViewAnimals = findViewById(R.id.textViewAnimals);
        btnRequest = findViewById(R.id.btnRequest);

        db = FirebaseFirestore.getInstance();

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Doesn't exist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnRequest.setOnClickListener(v -> sendRequest());

        loadPostDetails();
    }

    private void loadPostDetails() {
        db.collection("petSittingPosts").document(postId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        post = doc.toObject(PetSittingPost.class);
                        if (post != null) {
                            ownerId = post.ownerId;
                            textViewDate.setText(post.startDate + " - " + post.endDate);
                            textViewLocation.setText(post.location);
                            textViewNotes.setText(post.notes);
                            loadAnimals(post);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadAnimals(PetSittingPost post) {
        StringBuilder builder = new StringBuilder();
        for (String animalId : post.animalIds) {
            db.collection("users").document(post.ownerId).collection("Animals")
                    .document(animalId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        animal a = doc.toObject(animal.class);
                        if (a != null) {
                            builder.append("• ").append(a.getName()).append("\n");
                            textViewAnimals.setText(builder.toString());
                        }
                    });
        }
    }

    private void sendRequest() {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUser.equals(ownerId)) {
            Toast.makeText(this, "You can't send requests to your post!", Toast.LENGTH_SHORT).show();
            return;
        }

        String reqId = UUID.randomUUID().toString();
        PetSittingRequest req = new PetSittingRequest(reqId, postId, currentUser, "I want to help!");

        db.collection("petSittingRequests").document(reqId)
                .set(req)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
    }
}