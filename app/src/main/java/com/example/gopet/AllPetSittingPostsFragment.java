// AllPetSittingPostsFragment.java
package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_pet_sitting_posts, container, false);

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

        loadPosts();

        return view;
    }

    private void loadPosts() {
        db.collection("petSittingPosts")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        PetSittingPost post = doc.toObject(PetSittingPost.class);
                        if (!post.ownerId.equals(currentUser)) {
                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Eroare la încărcarea anunțurilor", Toast.LENGTH_SHORT).show();
                });
    }
}