package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity implements PetSittingRequestAdapter.OnRequestClickListener {

    private RecyclerView recyclerView;
    private PetSittingRequestAdapter adapter;
    private List<PetSittingRequest> requestList;
    private FirebaseFirestore db;
    private String postId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        initializeViews();
        setupFirestore();
        checkPostId();
        loadRequests();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        adapter = new PetSittingRequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void checkPostId() {
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "No ID found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRequests() {
        db.collection("petSittingRequests")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    requestList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        PetSittingRequest req = doc.toObject(PetSittingRequest.class);
                        if (req != null && "pending".equals(req.status)) {
                            req.id = doc.getId(); // Set document ID
                            fetchUsernameAndAddRequest(req);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading requests", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUsernameAndAddRequest(PetSittingRequest req) {
        if (req.userId == null || req.userId.isEmpty()) {
            return;
        }

        db.collection("users").document(req.userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String username = userDoc.getString("username");
                        req.message = "User: " + (username != null ? username : req.userId);
                        requestList.add(req);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAcceptClicked(PetSittingRequest selectedReq) {
        if (selectedReq == null || selectedReq.id == null) {
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("petSittingRequests")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Update all requests - accept selected one, reject others
                    for (DocumentSnapshot doc : snapshot) {
                        String docId = doc.getId();
                        String newStatus = docId.equals(selectedReq.id) ? "accepted" : "rejected";
                        db.collection("petSittingRequests")
                                .document(docId)
                                .update("status", newStatus);
                    }

                    // Update the post
                    db.collection("petSittingPosts")
                            .document(postId)
                            .update(
                                    "isActive", false,
                                    "acceptedUserId", selectedReq.userId
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Request accepted!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating post", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error processing request", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onUserProfileClicked(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening profile", Toast.LENGTH_SHORT).show();
        }
    }
}