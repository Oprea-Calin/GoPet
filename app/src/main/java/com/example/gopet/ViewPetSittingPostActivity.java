package com.example.gopet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ViewPetSittingPostActivity extends AppCompatActivity {

    private TextView textViewDate, textViewLocation, textViewPrice, textViewOwner,textViewAnimals;
    private Button btnRequest;
    private FirebaseFirestore db;
    private String postId, ownerId;
    private PetSittingPost post;
    private ImageView imageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pet_sitting_post);

        textViewDate = findViewById(R.id.textViewDate);
        textViewLocation = findViewById(R.id.textViewLocation);
        btnRequest = findViewById(R.id.btnRequest);
        imageProfile = findViewById(R.id.imageProfile);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewOwner= findViewById(R.id.textViewOwner);
        textViewAnimals = findViewById(R.id.textViewAnimals);


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

                            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
                            try {
                                Date start = inputFormat.parse(post.startDate);
                                Date end = inputFormat.parse(post.endDate);

                                String formattedStart = outputFormat.format(start);
                                String formattedEnd = outputFormat.format(end);

                                if (formattedStart.equals(formattedEnd)) {
                                    textViewDate.setText("Date: " + formattedStart);
                                } else {
                                    textViewDate.setText(formattedStart + " - " + formattedEnd);
                                }

                            } catch (ParseException e) {
                                textViewDate.setText(post.startDate + " - " + post.endDate);
                            }

                            textViewLocation.setText(post.aproximative_location);
                            loadAnimals(post);

                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            if (currentUserId.equals(ownerId)) {
                                btnRequest.setEnabled(false);
                                btnRequest.setText("Your post");
                            }

                            FirebaseFirestore.getInstance().collection("users")
                                    .document(post.ownerId)
                                    .get()
                                    .addOnSuccessListener(userdoc -> {
                                        String username = userdoc.getString("username");
                                        String base64Image = userdoc.getString("base64Image");
                                        post.ownerUsername = username;
                                        textViewOwner.setText("Posted by: " + username);

                                        if (post.price != null && !post.price.isEmpty()) {
                                            textViewPrice.setText("Pay: " + post.price);
                                        } else {
                                            textViewPrice.setText("Pay: to be discussed.");
                                        }

                                        if (base64Image != null && !base64Image.isEmpty()) {
                                            byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                            imageProfile.setImageBitmap(decodedByte);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }


    private void loadAnimals(PetSittingPost post) {
        StringBuilder animalDetails = new StringBuilder();
        textViewAnimals.setText("Pets loading");
        for (String animalId : post.animalIds) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(post.ownerId)
                    .collection("Animals").document(animalId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        animal a = doc.toObject(animal.class);
                        if (a != null) {
                            animalDetails.append("• ")
                                    .append(a.getName())
                                    .append(" - ")
                                    .append(a.getBreed())
                                    .append("\n");
                            textViewAnimals.setText(animalDetails.toString());
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

        db.collection("petSittingRequests")
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", currentUser)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "You have already sent a request for this post.", Toast.LENGTH_SHORT).show();
                    } else {
                        String reqId = UUID.randomUUID().toString();
                        PetSittingRequest req = new PetSittingRequest(reqId, postId, currentUser, "I want to help!");

                        db.collection("petSittingRequests").document(reqId)
                                .set(req)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error checking existing requests", Toast.LENGTH_SHORT).show());
    }

}