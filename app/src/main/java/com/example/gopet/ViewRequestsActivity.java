package com.example.gopet;

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

public class ViewRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PetSittingRequestAdapter adapter;
    private List<PetSittingRequest> requestList;
    private FirebaseFirestore db;
    private String postId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new PetSittingRequestAdapter(requestList, this::acceptRequest);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "No ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRequests();
    }

    private void loadRequests() {
        db.collection("petSittingRequests")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    requestList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        PetSittingRequest req = doc.toObject(PetSittingRequest.class);
                        if (req != null && req.status.equals("pending")) {
                            fetchUsernameAndAddRequest(req);
                        }
                    }
                });
    }

    private void fetchUsernameAndAddRequest(PetSittingRequest req) {
        db.collection("users").document(req.userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String username = userDoc.getString("username");
                        req.message = "Use: " + (username != null ? username : req.userId);
                        requestList.add(req);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void acceptRequest(PetSittingRequest selectedReq) {
        db.collection("petSittingRequests")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        PetSittingRequest req = doc.toObject(PetSittingRequest.class);
                        if (req != null) {
                            String newStatus = req.id.equals(selectedReq.id) ? "accepted" : "rejected";
                            db.collection("petSittingRequests")
                                    .document(req.id)
                                    .update("status", newStatus);
                        }
                    }

                    db.collection("petSittingPosts")
                            .document(postId)
                            .update("isActive", false,
                                    "acceptedUserId", selectedReq.userId);

                    Toast.makeText(this, "Request accepted!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
