package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MyPetSittingPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetSittingPostAdapter adapter;
    private List<PetSittingPost> postList;
    private FirebaseFirestore db;
    private String currentUser;
    private Button btnAddPost;
    private ProgressBar progressBar;

    private int totalExpectedPosts = 0;
    private int loadedPosts = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_pet_sitting_posts, container, false);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.recyclerViewMyPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        adapter = new PetSittingPostAdapter(getContext(), postList, post -> {
            Intent intent = new Intent(getContext(), ViewRequestsActivity.class);
            intent.putExtra("postId", post.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnAddPost = view.findViewById(R.id.btnAddPost);
        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreatePetSittingPostActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        postList.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        totalExpectedPosts = 0;
        loadedPosts = 0;

        loadMyPosts();
        loadAcceptedPosts();
    }

    private void loadMyPosts() {
        db.collection("petSittingPosts")
                .whereEqualTo("ownerId", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    totalExpectedPosts += docs.size();

                    for (DocumentSnapshot doc : docs) {
                        PetSittingPost post = doc.toObject(PetSittingPost.class);
                        postList.add(post);
                        incrementAndCheckDone();
                    }

                    if (docs.isEmpty()) {
                        incrementAndCheckDone();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Eroare la încărcarea anunțurilor proprii", Toast.LENGTH_SHORT).show();
                    incrementAndCheckDone();
                });
    }

    private void loadAcceptedPosts() {
        db.collection("petSittingRequests")
                .whereEqualTo("userId", currentUser)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(requests -> {
                    List<DocumentSnapshot> docs = requests.getDocuments();
                    totalExpectedPosts += docs.size();

                    if (docs.isEmpty()) {
                        incrementAndCheckDone();
                        return;
                    }

                    for (DocumentSnapshot reqDoc : docs) {
                        PetSittingRequest req = reqDoc.toObject(PetSittingRequest.class);
                        if (req != null) {
                            db.collection("petSittingPosts")
                                    .document(req.postId)
                                    .get()
                                    .addOnSuccessListener(postDoc -> {
                                        PetSittingPost post = postDoc.toObject(PetSittingPost.class);
                                        if (post != null && post.acceptedUserId != null) {
                                            db.collection("users")
                                                    .document(post.acceptedUserId)
                                                    .get()
                                                    .addOnSuccessListener(userDoc -> {
                                                        String name = userDoc.getString("username");
                                                        post.acceptedUsername = name;
                                                        postList.add(post);
                                                        incrementAndCheckDone();
                                                    })
                                                    .addOnFailureListener(e -> incrementAndCheckDone());
                                        } else {
                                            incrementAndCheckDone();
                                        }
                                    })
                                    .addOnFailureListener(e -> incrementAndCheckDone());
                        } else {
                            incrementAndCheckDone();
                        }
                    }
                })
                .addOnFailureListener(e -> incrementAndCheckDone());
    }

    private void incrementAndCheckDone() {
        loadedPosts++;
        if (loadedPosts >= totalExpectedPosts) {
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    }
}
