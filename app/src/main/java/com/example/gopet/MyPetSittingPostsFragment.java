// MyPetSittingPostsFragment.java
package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_pet_sitting_posts, container, false);

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

        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        db.collection("petSittingPosts")
                .whereEqualTo("ownerId", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        PetSittingPost post = doc.toObject(PetSittingPost.class);
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Eroare la încărcarea anunțurilor proprii", Toast.LENGTH_SHORT).show();
                });
    }
}
