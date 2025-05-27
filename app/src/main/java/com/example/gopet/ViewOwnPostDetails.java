package com.example.gopet;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ViewOwnPostDetails extends AppCompatActivity {

    private TextView textViewDate, textViewLocation, textViewPrice, textViewOwner;
    private ImageView imageProfile;
    private LinearLayout animalsContainer;
    private FirebaseFirestore db;
    private String postId, ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_own_post_details);

        textViewDate = findViewById(R.id.textViewDate);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewOwner = findViewById(R.id.textViewOwner);
        imageProfile = findViewById(R.id.imageProfile);
        animalsContainer = findViewById(R.id.animalsContainer);

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Missing post ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        loadPostDetails();
    }

    private void loadPostDetails() {
        db.collection("petSittingPosts").document(postId).get()
                .addOnSuccessListener(doc -> {
                    PetSittingPost post = doc.toObject(PetSittingPost.class);
                    if (post == null) return;

                    ownerId = post.ownerId;
                    loadPosterInfo(ownerId);

                    // Format date
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                        Date start = sdf.parse(post.startDate);
                        Date end = sdf.parse(post.endDate);
                        SimpleDateFormat out = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
                        textViewDate.setText(out.format(start) + " - " + out.format(end));
                    } catch (ParseException e) {
                        textViewDate.setText(post.startDate + " - " + post.endDate);
                    }

                    textViewLocation.setText("Location: " + post.location);
                    textViewPrice.setText("Pay: " + (post.price != null ? post.price : "To be discussed"));
                    loadAnimals(post);
                });
    }

    private void loadPosterInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");
                    String base64 = doc.getString("base64Image");

                    textViewOwner.setText("Posted by: " + username);

                    if (base64 != null && !base64.isEmpty()) {
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        imageProfile.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
                    }
                });
    }

    private void loadAnimals(PetSittingPost post) {
        animalsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String animalId : post.animalIds) {
            db.collection("users").document(post.ownerId)
                    .collection("Animals").document(animalId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        animal a = doc.toObject(animal.class);
                        if (a != null) {
                            View view = inflater.inflate(R.layout.item_animal, animalsContainer, false);

                            ((TextView) view.findViewById(R.id.itemName)).setText("Name: " + a.getName());
                            ((TextView) view.findViewById(R.id.itemAge)).setText("Age: " + a.getAge());
                            ((TextView) view.findViewById(R.id.itemBreed)).setText("Breed: " + a.getBreed());
                            ((TextView) view.findViewById(R.id.itemCategory)).setText("Type: " + a.getCategory());
                            ((TextView) view.findViewById(R.id.itemGender)).setText("Gender: " + a.getGender());
                            ((TextView) view.findViewById(R.id.itemWeight)).setText("Weight: " + a.getWeight());
                            ((TextView) view.findViewById(R.id.itemReproductiveStatus)).setText("Reproductive: " + a.getReproductiveStatus());
                            ((TextView) view.findViewById(R.id.itemAllergies)).setText("Allergies: " + a.getAllergies());

                            if (a.getBase64Image() != null && !a.getBase64Image().isEmpty()) {
                                byte[] decoded = Base64.decode(a.getBase64Image(), Base64.DEFAULT);
                                ((ImageView) view.findViewById(R.id.animalImageView))
                                        .setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
                            }

                            animalsContainer.addView(view);
                        }
                    });
        }
    }
}
