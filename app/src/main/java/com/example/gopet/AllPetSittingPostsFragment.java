package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AllPetSittingPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetSittingPostAdapter adapter;
    private List<PetSittingPost> postList;
    private FirebaseFirestore db;
    private String currentUser;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_pet_sitting_posts, container, false);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        adapter = new PetSittingPostAdapter(getContext(), postList, post -> {
            Intent intent = new Intent(getContext(), ViewPetSittingPostActivity.class);
            intent.putExtra("postId", post.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //loadPosts();

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        if (postList != null) {
            postList.clear();
        }
        loadPosts();

    }

    private void loadPosts() {
        db.collection("petSittingPosts")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();

                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    if (docs.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    final int[] loadedCount = {0};

                    for (DocumentSnapshot doc : docs) {
                        PetSittingPost post = doc.toObject(PetSittingPost.class);
                        db.collection("users").document(post.ownerId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String username = userDoc.getString("username");
                                        post.ownerUsername = username;
                                    }
                                    postList.add(post);
                                    loadedCount[0]++;

                                    if (loadedCount[0] == docs.size()) {
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }


    private void fetchOwnerAndAttach(PetSittingPost post) {
        db.collection("users").document(post.ownerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        if (username != null) {
                            post.notes = "Posted by: " + username + "\n" + post.notes;
                        }
                    }
                    postList.add(post);
                    adapter.notifyDataSetChanged();
                });
    }
}